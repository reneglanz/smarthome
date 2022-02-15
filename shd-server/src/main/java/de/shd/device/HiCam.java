package de.shd.device;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.ftp.IFtpFileHandler;
import de.core.http.Http;
import de.core.http.HttpResponse;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.annotation.Element;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.core.utils.Streams;
import de.shd.device.data.SwitchData;
import de.shd.device.data.TaskData;

public class HiCam extends Camera implements Switch, Launchable, IFtpFileHandler, Releasable {

	@Element public String captureDir;
	@Element public String host;
	@Element public String user;
	@Element public String password;
	@Element public String ftpUser;

	private Switch.State state = Switch.State.UNKNOWN;
	protected String taskUrn=null;
	
	public ExportData createExportData() {
		if(taskUrn==null) {
			try {
			ServiceProvider<?> provider=Services.getProvider(this);
			if(provider!=null) {
				String h=provider.getProviderId();
				taskUrn=h+":"+getDeviceId()+":getImage";
			}
			} catch(Throwable t) {};
		}
		return new ExportData(getDeviceId(), name, new SwitchData(this.state), new TaskData(imageRefreshRate, taskUrn));
	}

	public Switch.State toggle() throws CoreException {
		if (this.state == Switch.State.ON) {
			this.state = Switch.State.OFF;
		} else {
			this.state = Switch.State.ON;
		}
		setState(this.state);
		export();
		return this.state;
	}

	public Switch.State getState() throws CoreException {
		return this.state;
	}

	public Switch.State setState(Switch.State state) throws CoreException {
		String value = (state == Switch.State.ON) ? "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP"
				: "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN";
		String url = "http://" + this.host + "/web/cgi-bin/hi3510/param.cgi";
		String data = "cmd=setplanrecattr&cururl=http%3A%2F%2F127.0.0.1%2Fweb%2Fscheduleex.html&recswitch=&recstream=&planrec_time=&cmd=setscheduleex&-ename=md";
		for (int i = 0; i < 7; i++)
			data = data + "&-week" + i + "=" + value;
		try {
			Http.post(url, getHeader(), data.getBytes());
		} catch (Exception e) {
			CoreException.throwCoreException(e);
		}
		data = "cmd=setinfrared&cururl=http%3A%2F%2F127.0.0.1%2Fweb%2Fdisplay.html&-infraredstat="
				+ ((state == Switch.State.ON) ? "auto" : "close");
		try {
			Http.post(url, getHeader(), data.getBytes());
		} catch (Exception e) {
			CoreException.throwCoreException(e);
		}
		return state;
	}

	private void updateState() {
		try {
			HttpResponse resp = Http.get(
					"http://" + this.host + "/web/cgi-bin/hi3510/param.cgi?cmd=getlanguage&cmd=getscheduleex&-ename=md",
					getHeader());
			byte[] data = Streams.readAll(resp.getContent());
			parseSchedules(new String(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> getHeader() throws UnsupportedEncodingException {
		Map<String, String> header = new HashMap<>();
		header.put("Authorization",
				"Basic " + Base64.getEncoder().encodeToString((this.user + ":" + this.password).getBytes("UTF-8")));
		return header;
	}

	private void parseSchedules(String response) {
		String[] lines = response.split("\r\n");
		boolean[] active = new boolean[7];
		int counter = 0;
		for (String line : lines) {
			if (line.startsWith("var week")) {
				if (line.contains("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP")) {
					active[counter] = true;
				} else {
					active[counter] = false;
				}
				counter++;
			}
		}
		boolean a0 = true;
		for (boolean b : active)
			a0 &= b;
		if (a0) {
			this.state = Switch.State.ON;
		} else {
			this.state = Switch.State.OFF;
		}
	}

	public void launch() throws CoreException {
		updateState();
		registerFtpFileHandler();
	}

	public boolean canHandle(String user, String file) {
		return this.ftpUser.equals(user);
	}

	public void onFile(Path path) {
	}

	@Override
	public void release() throws CoreException {
		deregisterFtpFileHandler();
	}
}
