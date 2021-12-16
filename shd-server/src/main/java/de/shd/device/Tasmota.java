package de.shd.device;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Tasmota extends MqttSwitchDevice implements PowerMeter {
	/**
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
	 * @author Rene
	 *
	 */
	private class TasmoteTele implements Serializable {
		@Element(name="Time") String time;
		@Element(name="ENERGY") Energy energy;
	}
	
	private class Energy implements Serializable {
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
	
	protected TasmoteTele tele;

	@Override
	protected void initSubscriber() {
		this.subscriber=new MqttSubscriber(baseTopic+tasmotaId+"/#") {
			
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				if(topic.endsWith("POWER")) {
					String data = new String(message.getPayload());
					Tasmota.this.state = Tasmota.this.mapState(data);
					if (Tasmota.this.store != null)
						Tasmota.this.store.add(Tasmota.this.store.parseData(message.getPayload()));
					Tasmota.this.export();
				} else if(topic.endsWith("tele/SENSOR")) {
					tele=Coding.decode(message.getPayload(),"json",TasmoteTele.class);
				}
			}
		};
		super.initSubscriber();
	}
	
	

	@Override
	public void launch() throws CoreException {
		this.publishTopic=baseTopic+tasmotaId+"/cmnd/POWER";
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

}
