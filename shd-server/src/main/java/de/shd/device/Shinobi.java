package de.shd.device;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.core.CoreException;
import de.core.data.Data;
import de.core.http.Http;
import de.core.http.HttpResponse;
import de.core.rt.Launchable;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.service.Function;
import de.core.service.Param;
import de.core.task.IntervalTask;
import de.core.task.Scheduler;
import de.core.utils.Streams;
import de.shd.device.data.SwitchData;
import de.shd.device.data.TaskData;
import de.shd.device.data.TextData;
import de.shd.device.data.VideoStreamingData;

public class Shinobi extends Camera implements Switch,Launchable {
	
	public static class VideoList implements Serializable {
		@Element boolean ok;
		@Element Video[] videos;
	}

	public static class Video implements Serializable {
		public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		public static DateFormat dfUI = new SimpleDateFormat("MM.dd.yyyy hh:mm:ss");
		
		@Element String mid;
		@Element String time;
		@Element long size;
		@Element int status;
		@Element String filename;
		@Element String href;
	}
	
	@Element String host;
	@Element String apikey;
	@Element String groupid;
	@Element String cameraid;
	
	@Element(defaultValue="MOTIONON") String presetMotionOn="MOTIONON";
	@Element(defaultValue="MOTIONOFF") String presetMotionOff="MOTIONOFF";
	
	@Element int syncStateInteval=30000;
	@Injectable Scheduler scheduler;
	
	protected String baseurl="http://"+host+"/"+apikey;
	protected State state=State.UNKNOWN;
	
	protected int detectedMotions=0;
	
	public Shinobi() {}
	
	@Function
	public int getEventNumber() {
		return 1;
	}
	
	@Function
	public void setPreset(@Param("preset") String preset) throws CoreException {
		String url=baseurl+"/monitorStates/"+groupid +"/"+preset;
		try {
			byte[]result=Http.get(url);
			System.out.println(new String(result));
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
	}
	
	@Override
	public State toggle() throws CoreException {
		return state==State.ON?setState(State.OFF):setState(State.ON);
	}

	@Override
	public State getState() throws CoreException {
		String url=baseurl+"/monitor/"+groupid +"/"+cameraid;
		try {
			byte[]result=Http.get(url);
			String s=new String(result);
			if(s.contains("\"detector\\\":\\\"0\\\"")) {
				this.state=State.OFF;
			} else if (s.contains("\"detector\\\":\\\"1\\\"")) {
				this.state=State.ON;
			} else {
				this.state=State.UNKNOWN;
			}
			return state;
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}
	
	@Function
	public Video[] getRecordings() throws CoreException {
		String url=baseurl+"/videos/"+groupid+"/"+cameraid;
		try {
			VideoList videos=Coding.decode(Http.get(url), "json",VideoList.class);
			Arrays.stream(videos.videos).forEach((Video v)->{v.href="http://"+host+v.href;
				try {
					v.time=Video.dfUI.format(Video.df.parse(v.time.substring(0, v.time.length()-6)));
				} catch (Throwable t) {}
			});
			detectedMotions=videos.videos.length;
			return videos.videos;
		} catch(Throwable t) {
			CoreException.throwCoreException(t);
		}
		return new Video[0];
	}

	@Override
	public State setState(State state) throws CoreException {
		if(state==State.ON) {
			this.setPreset(presetMotionOn);
			this.state=state;
		} else {
			this.setPreset(presetMotionOff);
			this.state=state;	
		}
		return null;
	}
	
	@Function
	public synchronized void motion() throws CoreException {
		detectedMotions++;
		export();
	}

	@Override
	public void launch() throws CoreException {
		baseurl="http://"+host+"/"+apikey;
		this.getState();
		this.getRecordings();
		if(this.scheduler!=null) {
			scheduler.schedule(new IntervalTask(syncStateInteval) {
				@Override
				public void execute() throws CoreException {
					Shinobi.this.state=Shinobi.this.getState();
					Shinobi.this.getRecordings();
				}
			});
		}
	}

	@Override
	public ExportData createExportData() {
		List<Data>data=new ArrayList<>();
		if(imageUrl!=null) {
			data.add(new TaskData(imageRefreshRate, "devices:"+this.getDeviceId().toString()+":getImage"));
		}
		if(videoUrl!=null) {
			data.add(new VideoStreamingData(this.videoUrl,this.videoType));
		}
		data.add(new SwitchData(this.state));
		data.add(new TextData("Bewegungserkennung " + (this.state==state.ON?"an":"aus")+(detectedMotions>0?" - "+detectedMotions+" Ereignisse":"")));
		return new ExportData(getDeviceId(), name, data.toArray(new Data[data.size()]));
	}
	
	@Function
	public void delete(@Param("video") Video video){
		try {
			HttpResponse resp=Http.get(Http.buildUrl(baseurl,"videos",groupid,cameraid,video.filename,"delete"), null);
			if(resp.getStatusCode()!=200) {
				throw new CoreException("Delete failed");
			}
			
		} catch(Throwable t) {
			CoreException.throwCoreException(t);
		}
	}
}
