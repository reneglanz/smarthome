package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.Env;
import de.core.mqtt.MqttClient;
import de.core.mqtt.MqttSubscriber;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttTrigger extends AbstractTrigger implements Launchable, Releasable {
  @Element
  protected String topic;
  
  @Injectable
  protected MqttClient mqtt;
  
  private MqttSubscriber subscriber;
  
  protected MqttTrigger() {}
  
  public MqttTrigger(String topic) {
    this.topic = topic;
  }
  
  public void launch() throws CoreException {
    if (this.mqtt == null)
      this.mqtt = (MqttClient)Env.get(MqttClient.class); 
    if (this.mqtt != null) {
      this.subscriber = new MqttSubscriber(this.topic) {
          public void messageArrived(String arg0, MqttMessage msg) throws Exception {
            MqttTrigger.this.automation.getData().set("mqtt."+topic, new String(msg.getPayload()));
            MqttTrigger.this.runAutomation();
          }
        };
      this.mqtt.subscribe(this.subscriber);
    } 
  }
  
  public void release() throws CoreException {
    if (this.mqtt != null && this.subscriber != null)
      this.mqtt.unsubscribe(this.subscriber); 
  }
}
