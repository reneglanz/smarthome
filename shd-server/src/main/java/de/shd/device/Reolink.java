package de.shd.device;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.core.CoreException;
import de.core.ftp.IFtpFileHandler;
import de.core.http.Http;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.DefaultResponse;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.http.handler.HttpRequestHandler;
import de.core.log.Logger;
import de.core.rt.Launchable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.serialize.elements.Array;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.PrimitivElement;
import de.core.serialize.parser.CodingReadHandler;
import de.core.serialize.parser.DefaultReadHandler;
import de.core.serialize.parser.JsonParser;
import de.core.serialize.writer.DefaultWriter;
import de.core.serialize.writer.JsonWriter;
import de.core.task.IntervalTask;
import de.core.task.Scheduler;
import de.core.utils.Exec;
import de.core.utils.FileUtils;
import de.shd.device.data.TaskData;
import de.shd.device.data.TextData;

public class Reolink extends AbstractCamera implements IFtpFileHandler, Launchable {

	private class ReolinkRequest extends ComplexElement {
		String cmd;
		Token token;
		
		public ReolinkRequest(String cmd, Token token) {
			super(null);
			this.cmd=cmd;
			this.token=token;
			this.add(new PrimitivElement(this, "cmd", cmd));
		}
		
		public byte[] getAsBytes() throws CoreException {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			DefaultWriter writer=new DefaultWriter();
			writer.skipType=true;
			Array arr=new Array(null);
			this.setParent(arr);
			arr.add(this);
			writer.write(arr, baos, new JsonWriter());
			return baos.toByteArray();
		}
		
		private ComplexElement send() {
			try {
				if(log.isDebug()) log.debug("["+id+"] - send request: " + cmd + ": " + new String(getAsBytes()));
				byte[] ba=Http.post(getUrl(cmd, token), null,getAsBytes());
				String resp=new String(ba,"UTF-8");
				if(log.isDebug()) log.debug("["+id+"] - recv response: " + cmd + ": " + resp);
				if(resp.length()>0) {
					resp=resp.substring(1,resp.length()-1).trim();
					ComplexElement e=parseResponse(resp.getBytes());
					String error=getError(e);
					if(error!=null) {
						throw new CoreException(error);
					}
					return e;
				} 
			} catch(Throwable t) {
				CoreException.throwCoreException(t);
			}
			return null;
		}

		
		public String getError(ComplexElement e) {
			int code=e.getPrimitiveChild("code").asInteger();
			if(code==1) {
				return e.getComplexChild("error").getPrimitiveChild("detail").asString();
			} 
			return null;
		}
	}
	
	
	public class RequestFactroy {
		public ReolinkRequest loginRequest(String user,String password) {
			ReolinkRequest request=new ReolinkRequest("Login", null);
			ComplexElement userElement=new ComplexElement(request);
			userElement.setName("User");
			userElement.add(new PrimitivElement(userElement, "userName", user));
			userElement.add(new PrimitivElement(userElement, "password", password));
			ComplexElement param=new ComplexElement(request, "param");
			param.add(userElement);
			request.add(param);
			return request;
		}
		
		public ReolinkRequest getRecRequest() {
			ReolinkRequest request=new ReolinkRequest("GetRec", getToken());
			request.add(new PrimitivElement(request, "action", 1));
			ComplexElement param=new ComplexElement(request,"param");
			param.add(new PrimitivElement(param, "channel", 0));
			request.add(param);
			return request;
		}
		
		public ReolinkRequest getFtpUploadRequest() {
			ReolinkRequest request=new ReolinkRequest("GetFtp",getToken());
			request.add(new PrimitivElement(request, "action", 1));
			return request;
		}
		
		public ReolinkRequest getSetRecRequest(State recordingState) {
			ReolinkRequest request=new  ReolinkRequest("SetRec",getToken());
			ComplexElement param=new ComplexElement(request,"param");
			ComplexElement rec=new ComplexElement(param,"Rec");
			rec.add(new PrimitivElement(rec,"channel",0));
			rec.add(new PrimitivElement(rec,"overwrite",1));
			rec.add(new PrimitivElement(rec,"postRec","30 Seconds"));
			rec.add(new PrimitivElement(rec,"preRec",1));
			ComplexElement schedule=new ComplexElement(rec,"schedule");
			schedule.add(new PrimitivElement(schedule,"enable",recordingState==State.ON?1:0));
			schedule.add(new PrimitivElement(schedule,"table","111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"));
			rec.add(schedule);
			param.add(rec);
			request.add(param);
			return request;
		}
		
		public ReolinkRequest setFtpUploadRequest(State ftpUploadState) {
			ReolinkRequest request=new  ReolinkRequest("SetFtp",getToken());
			ComplexElement param=new ComplexElement(request,"param");
			ComplexElement rec=new ComplexElement(param,"Ftp");
			ComplexElement schedule=new ComplexElement(rec,"schedule");
			schedule.add(new PrimitivElement(schedule,"enable",ftpUploadState==State.ON?1:0));
			schedule.add(new PrimitivElement(schedule,"table","111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"));
			rec.add(schedule);
			param.add(rec);
			request.add(param);
			return request;
		}
	}
		
	public static class Token  {
		int leaseTime;
		String name;
		long created=System.currentTimeMillis();
		
		public boolean isValid() {
			return (created+(leaseTime*1000)-360)>System.currentTimeMillis();
		}
		
		public String toString() {
			return name;
		}
	}
	
	public class RecordingRequestHandler extends AbstractHttpRequestHandler {
		Pattern RANGE_PATTERN=Pattern.compile("bytes=(\\d*)-(\\d*)");
		
		public RecordingRequestHandler() {
			super("/camera/"+id+"/recordings");
		}
		
		@Override
		public HttpResponse handleRequest(HttpRequest request) {
			String requestedFile=request.getRequestPath();
			requestedFile=requestedFile.substring(requestedFile.lastIndexOf("/")+1,requestedFile.length());
			Recording recording=getRecording(requestedFile);
			if(recording!=null&&Files.exists(recording.getFile())) {
				try {
					int leng=(int) Files.size(recording.getFile());
					int start=0;
					int end=leng-1;
					HttpHeader range=request.getHeader("Range");
					if(range!=null) {
						
						Matcher matcher=RANGE_PATTERN.matcher(range.getValue());
						if(matcher.matches()) {
							String startGroup=matcher.group(1);
							start=startGroup.isEmpty()?start:Integer.valueOf(startGroup);
							start=start<0?0:start;
							String endGroup=matcher.group(2);
							end=endGroup.isEmpty()?leng-1:end;
						}
						log.debug("Range Header found "+String.format("bytes %s-%s/%s", start,end,leng));
					}
					int contentlength=end-start+1;
					DefaultResponse response=new DefaultResponse();
					//response.addHeader("Content-Disposition", "inline;filename=recording.mp4");
					response.addHeader("Accept-Ranges", "bytes");
					response.addHeader("Last-Modified",Long.toString(Files.getLastModifiedTime(recording.getFile()).toMillis()));
					response.addHeader("Expires",Long.toString(System.currentTimeMillis()+1000*60*20));
					response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, "video/mp4"));
					response.addHeader("Content-Range", String.format("bytes %s-%s/%s", start,end,leng));
					response.setLength(contentlength);
					try {
						SeekableByteChannel input=Files.newByteChannel(recording.getFile(),StandardOpenOption.READ);
						input.position(0);
						response.setIs(Channels.newInputStream(input));
						response.setStatusCode(206);
					} catch (Exception e) {
					}
					return response;
				} catch (Throwable t) {
					return new FixedLengthHttpResponse(t.getMessage().getBytes(), 500);
				}
			} else {
				return new FixedLengthHttpResponse("File not found".getBytes(), 404);
			}
		}

		@Override
		public boolean keepAlive() {
			return false;
		}
	}
	
	@Element protected String host;
	@Element protected String user;
	@Element protected String password;
	@Element protected String recordingPath;
	@Element protected String videoBaseUrl;
	@Element(defaultValue="10000") protected int syncStatus=10000;
	
	private static final Logger log=Logger.createLogger("Reolink");
	protected Token token;
	protected State recording=State.UNKNOWN;
	protected State ftpUpload=State.UNKNOWN;
	protected List<Recording> recordings=Collections.synchronizedList(new ArrayList<>());
	protected HttpRequestHandler httpHandler;
	@Injectable  Scheduler scheduler;
	
	@Override
	public State setRecordingState(State state) throws CoreException {
		this.recording=state;
		ReolinkRequest request=new RequestFactroy().getSetRecRequest(state);
		request.send();
		this.setFtpUpdateState(state);
		export();
		return this.recording;
	}
	
	public State setFtpUpdateState(State state) throws CoreException {
		this.ftpUpload=state;
		ReolinkRequest request=new RequestFactroy().setFtpUploadRequest(state);
		request.send();
		return ftpUpload;
	}

	@Override
	public void setInfraRedState(InfraRedState irState) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InfraRedState getInfraRedState() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State getRecordingState() throws CoreException {
		ReolinkRequest request=new RequestFactroy().getRecRequest();
		ComplexElement root=request.send();
		int enabled=root.getComplexChild("value").getComplexChild("Rec").getComplexChild("schedule").getPrimitiveChild("enable").asInteger();
		this.recording=enabled==1?State.ON:State.OFF;
		if(recording!=ftpUpload) {
			setFtpUpdateState(recording);
		}
		
		return recording;
	}
	
	public State getFtpUploadState() throws CoreException {
		ReolinkRequest request=new RequestFactroy().getFtpUploadRequest();
		ComplexElement root=request.send();
		int enabled=root.getComplexChild("value").getComplexChild("Ftp").getComplexChild("schedule").getPrimitiveChild("enable").asInteger();
		this.ftpUpload=enabled==1?State.ON:State.OFF;
		return ftpUpload;
	}

	private String getUrl(String command, Token token) {
		return "http://"+host+"/cgi-bin/api.cgi?cmd="+command+(token!=null?"&token="+token.name:"");
	}
	
	public Token getToken() {
		if(this.token==null||!this.token.isValid()) {
			try {
				synchronized (this) {
					if(this.token==null||!this.token.isValid()) {
						log.debug("["+id+"] - Get new token");
						ReolinkRequest request=new RequestFactroy().loginRequest(user, password);
						ComplexElement root=request.send();
						Token token=new Token();
						token.name=root.getComplexChild("value").getComplexChild("Token").getPrimitiveChild("name").asString();
						token.leaseTime=root.getComplexChild("value").getComplexChild("Token").getPrimitiveChild("leaseTime").asInteger();
						this.token=token;
					}
				}
			} catch (Throwable e) {
				log.error("Failed to create token", e);
			}
		} 

		return token;
	}
	
	@Override
	public ExportData createExportData() {
		ExportData data=new ExportData(getDeviceId(),name,
									   new TextData("Aufnahme " + (this.recording==recording.ON?"an":"aus")+(recordings.size()>0?" - "+recordings.size()+" Ereignisse":"")),
									   new TaskData(10000, "devices:"+id+":getImage"));
		return data;
	}

	private ComplexElement parseResponse(byte[] ba) throws CoreException {
		try {
			JsonParser parser=new JsonParser();
			DefaultReadHandler handler = new DefaultReadHandler();
			parser.parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ba), "UTF-8")), (CodingReadHandler)handler);
		    return handler.getResult();
		} catch(Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}
	
	@Override
	public List<Recording> getRecordings() throws CoreException {
		Collections.sort(recordings);
		return recordings;
	}
	
	public void searchRecording() throws CoreException {
		if(this.recordingPath!=null){
			Path recDir=Paths.get(recordingPath);
			try (Stream<Path>filewalk=Files.walk(recDir)){
				filewalk.forEach((p)->{
					addRecording(p);
				});
			} catch (IOException e) {
				log.error("Failed to list recordings from " + recordingPath,e);
			}
		} else {
			this.recordings=Collections.emptyList();
		}
	}

	private void addRecording(Path p) {
		if(!Files.isDirectory(p)&&p.getFileName().toString().endsWith("mp4")) try {
			Recording r=new Recording();
			r.file=p;	
			r.date=Files.getLastModifiedTime(r.getFile()).toMillis();
			r.size=Files.size(r.getFile());
			r.contentType="mp4";
			r.createId();
			r.url=videoBaseUrl+(videoBaseUrl.endsWith("/")?"":"/")+r.getId();
		
			Path thumbnail=Paths.get(FileUtils.getFileNameWithoutExtension(p)+".jpg");
			if(!Files.exists(thumbnail)) {
				createThumbnail(p, thumbnail);
			}
			if(Files.exists(thumbnail)) {
				r.thumbnail=getDataUrl(Files.readAllBytes(thumbnail));
			}
			if(getRecording(r.getId())==null) {
				recordings.add(r);
			}
		} catch (Throwable t) {
			t=t;
		}
	}
	
	private void createThumbnail(Path video, Path thumbnail) {
		Exec exec=new Exec("ffmpeg","-i",video.toString(),"-vframes","1","-s","400x222", thumbnail.toString());
		exec.sync();
	}
	
	public Recording getRecording(String id) {
		for(Recording r:recordings){
			if(r.getId().equals(id)) {
				return r;
			}
		}
		return null;
	}
	
	@Override
	public boolean canHandle(String paramString1, String paramString2) {
		return true;
	}

	@Override
	public void onFile(Path paramPath) {
		addRecording(paramPath);
	}

	@Override
	public State toggleRecording() throws CoreException {
		if(this.recording==State.ON) {
			return setRecordingState(State.OFF);
		} else {
			return setRecordingState(State.ON);
		}
	}

	@Override
	public void deleteRecording(Recording recording) throws CoreException {
		Recording r=getRecording(recording.getId());
		if(r!=null) {
			if(Files.exists(r.getFile()))try {
				Files.delete(r.getFile());
			} catch (IOException e) {
				CoreException.throwCoreException(e);
			}
			recordings.remove(r);
		}
	}

	@Override
	public void deleteAllRecordings() throws CoreException {
		for(int i=this.recordings.size()-1;i>=0;i--){
			deleteRecording(this.recordings.get(i));
		}
		export();
	}
	
	@Override
	public void launch() throws CoreException {
		this.registerFtpFileHandler();
		this.httpHandler=new RecordingRequestHandler();
		this.httpHandler.registerHttpRequestHandler();
		this.searchRecording();
		if(this.scheduler!=null&&this.syncStatus>0) {
			scheduler.schedule(new IntervalTask(syncStatus) {
				@Override
				public void execute() throws CoreException {
					getRecordingState();
					getFtpUploadState();
					export();
				}
			});
		}
	}
}
