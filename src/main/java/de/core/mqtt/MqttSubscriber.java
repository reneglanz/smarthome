package de.core.mqtt;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

public abstract class MqttSubscriber implements IMqttMessageListener, Serializable {
  @Element
  protected String topic;
  
  @Element
  protected int qos;
  
  protected MqttSubscriber() {}
  
  protected MqttSubscriber(String topic) {
    this.topic = topic;
  }
}
