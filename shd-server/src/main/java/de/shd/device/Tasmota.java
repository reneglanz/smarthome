package de.shd.device;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.device.data.CurrentPowerData;

public class Tasmota extends MqttSwitchDevice implements PowerMeter {
	/**
	 * TELE
	 * {
		   "Time":"2021-07-26T09:11:00",
		   "ENERGY":{
		      "TotalStartTime":"2020-10-04T09:42:16",
		      "Total":40.543,
		      "Yesterday":0.000,
		      "Today":0.140,
		      "Period":8,
		      "Power":91,
		      "ApparentPower":144,
		      "ReactivePower":111,
		      "Factor":0.63,
		      "Voltage":229,
		      "Current":0.627
		   }
		}
		
		State
		{
		  "Time": "2022-04-22T18:42:56",
		  "Uptime": "33T00:18:21",
		  "UptimeSec": 2852301,
		  "Heap": 26,
		  "SleepMode": "Dynamic",
		  "Sleep": 50,
		  "LoadAvg": 19,
		  "MqttCount": 22,
		  "POWER": "ON",
		  "Wifi": {
		    "AP": 1,
		    "SSId": "FB-7360-GL",
		    "BSSId": "E8:DF:70:D0:ED:70",
		    "Channel": 1,
		    "Mode": "11n",
		    "RSSI": 62,
		    "Signal": -69,
		    "LinkCount": 16,
		    "Downtime": "7T02:57:32"
		  }
		}
		
	 * @author Rene
	 *
	 */
	
	public static class TasmotaState implements Serializable {
		@Element(name="Time") String time;
		@Element(name="Uptime") String uptime;
		@Element(name="POWER") String power;
	}
	
	public static class TasmotaTele implements Serializable {
		@Element(name="Time") String time;
		@Element(name="ENERGY") Energy energy;
	}
	
	public static class Energy implements Serializable {
		@Element(name="TotalStartTime") String time;
		@Element(name="Total") float total;
		@Element(name="Yesterday") float yesterday;
		@Element(name="Today") float today;
		@Element(name="Period") int priod;
		@Element(name="Power") int power;
		@Element(name="ApparentPower") float apparentPower;
		@Element(name="ReactivePower") float reactivePower;
		@Element(name="Factor") float factor;
		@Element(name="Voltage") float voltage;
		@Element(name="Current") float current;
	}
	
	
	@Element(defaultValue="/home/sockets/") String baseTopic="/home/sockets/";
	@Element String tasmotaId;
	
	protected TasmotaTele tele;
	protected TasmotaState tasmotaState;

	@Override
	protected void initSubscriber() {
		this.subscriber=new MqttSubscriber(baseTopic+tasmotaId+"/#") {
			
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				if(topic.endsWith("POWER")) {
					String data = new String(message.getPayload());
					Tasmota.this.state = Tasmota.this.mapState(data);
				} else if(topic.endsWith("tele/SENSOR")) {
					tele=Coding.decode(message.getPayload(),"json",TasmotaTele.class);
				} else if(topic.endsWith("tele/STATE")) {
					tasmotaState=Coding.decode(message.getPayload(),"json",TasmotaState.class);
					if(tasmotaState!=null) {
						Tasmota.this.state=mapState(tasmotaState.power);
					}
				}
				Tasmota.this.export();
			}
		};
		if(mqttClient!=null) try {
			this.mqttClient.subscribe(subscriber);
		} catch (CoreException e) {}
	}
	
	

	@Override
	public void launch() throws CoreException {
		this.publishTopic=baseTopic+tasmotaId+"/cmnd/POWER";
		super.launch();
	}



	@Override
	public int currentPower() {
		if(tele!=null){
			return tele.energy.power;
		}
		return 0;
	}

	@Override
	public float today() {
		if(tele!=null){
			return tele.energy.today;
		}
		return 0;
	}

	@Override
	public float yesterday() {
		if(tele!=null){
			return tele.energy.yesterday;
		}
		return 0;
	}

	@Override
	public float total() {
		if(tele!=null){
			return tele.energy.total;
		}
		return 0;
	}

	@Override
	public Integer readValue() {
		return new Integer(currentPower());
	}
	
	public ExportData createExportData() {
		return super.createExportData().addData(new CurrentPowerData(currentPower()));
	}
}
