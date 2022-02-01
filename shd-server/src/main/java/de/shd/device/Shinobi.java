package de.shd.device;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.http.Http;
import de.core.rt.Launchable;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.core.task.IntervalTask;
import de.core.task.Scheduler;
import de.shd.device.data.SwitchData;
import de.shd.device.data.TaskData;
import de.shd.device.data.TextData;

public class Shinobi extends Camera implements Switch,Launchable {
	
	public static class VideoList implements Serializable {
		@Element boolean ok;
		@Element Video[] videos;
	}

	public static class Video implements Serializable {
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
	
	public void getRecordings() throws CoreException {
		String url=baseurl+"/videos/"+groupid+"/"+cameraid;
		try {
			VideoList videos=Coding.decode(Http.get(url), "json",VideoList.class);
			detectedMotions=videos.videos.length;
		} catch(Throwable t) {
			CoreException.throwCoreException(t);
		}
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
		TaskData task=null;
		if(imageUrl!=null) {
			task=new TaskData(imageRefreshRate, "devices:"+this.getDeviceHandle().toString()+":getImage");
		}
		return new ExportData(getDeviceHandle(), name, new SwitchData(this.state), new TextData(detectedMotions +" Bewegungen erkannt"),task);
	}
	
	public static void main(String[] args) throws CoreException, IOException {
		Shinobi s=Coding.decode(Files.readAllBytes(Paths.get("/home/rene/workspace/eclipse/deploy/config/devices/camera-terasse.device.sjos")));
		s=s;
		
		System.out.println(new String(Coding.encode(new Shinobi())));
	}
	
}
