package de.shd.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.shd.update.UpdateService;

public class ValveControler extends MqttDevice {

	public static class Data implements de.core.data.Data {
		@Element String name;
		@Element int state;
		@Element int duration;
		@Element int lastrun;
		
		protected Data() {}
		
		public State getState() {
			if(state==1)
				return State.ON;
			else 
				return State.OFF;
		}
	}
	
	public static class Cmd implements Serializable {
		@Element String name;
		@Element int state;
		@Element int duration;
		
		private Cmd(String name,int state,int duration) {
			this.name=name;
			this.state=state;
			this.duration=duration;
		}
	}
	
	private class Valve extends AbstractDevice implements Switch, Range {

		Data exportData;
		String publishTopic;
				
		private Valve(String id,String name,String topic) {
			this.publishTopic=topic+"/cmd";
			this.id=id;
			this.name=name;
		}
		
		@Override
		public State toggle() throws CoreException {
			if(this.exportData.state==1) {
				this.exportData.state=0;
			} else {
				this.exportData.state=1;
			}
			publish(new Cmd(id, this.exportData.state, -1));
			return null;
		}

		@Override
		public void setRange(int value) throws CoreException {
			this.exportData.state=1;
			this.exportData.duration=value;
			publish(new Cmd(id, this.exportData.state, value));
		}

		@Override
		public State getState() throws CoreException {
			return exportData!=null?exportData.getState():State.UNKNOWN;
		}

		@Override
		public State setState(State state) throws CoreException {
			if(exportData.getState()!=state) {
				exportData.state=state==State.ON?1:0;
			}
			publish(new Cmd(name, state==State.ON?1:0, -1));
			return exportData.getState();
		}

		@Override
		public ExportData createExportData() {
			return new ExportData(this.getDeviceId(), name, exportData);
		}
		
		private void publish(Cmd cmd) throws CoreException {
			try {
				ValveControler.this.mqttPublish(publishTopic,new String(Coding.encode(cmd, "json", true),"UTF-8"));
				export();
			} catch (Exception e) {
				CoreException.throwCoreException(e);
			}
		}

		@Override
		public int getRange() throws CoreException {
			return exportData.duration;
		}
		
	}
	
	@Element Map<String,String>names=new HashMap<String, String>();
	HashMap<String, Valve> valves=new HashMap<String, ValveControler.Valve>();
	
	@Override
	protected void initSubscriber() {
		this.subscriber=new MqttSubscriber("/home/garden/water/valves/+") {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				Valve valve=valves.get(topic);
				if(valve==null) {
					valve=create(topic);
				}
				valve.exportData=Coding.decode(message.getPayload(), "json", Data.class);
				export(valve);
			}
		};
		
		if(mqttClient!=null) try {
			mqttClient.subscribe(subscriber);
		} catch(CoreException e) {}
	}
	
	protected Valve create(String topic) throws CoreException {
		String id=topic.substring(topic.lastIndexOf("/")+1);
		Valve valve=new Valve(id,names.containsKey(id)?names.get(id):id,topic);
		valves.put(topic,valve);
		ServiceProvider<?> provider=Services.getProvider(this);
		Services.bind(provider!=null?provider.getProviderId():null, valve);
		return valve;
	}
	
	
	@Override
	public ExportData createExportData() {
		return null;
	}
	
	@Override
	public void export()throws CoreException {
		for(Entry<String,Valve>entry:valves.entrySet()) {
			export(entry.getValue());
		}
	}
	
	
	public void export(Valve valve) throws CoreException {
		if(updateService==null) {
			updateService=Services.get(UpdateService.class);
		}
		if(updateService!=null) {
			updateService.update(valve.createExportData());
		}
	}

}
