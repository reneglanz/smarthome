package de.shd.device;

import de.core.CoreException;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.Coding;
import de.core.serialize.annotation.Element;
import java.util.HashMap;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ShellyDuo extends MqttSwitchDevice implements Light {
  @Element
  String shellyId;
  
  protected StatusData data;
  
  public static class Data implements de.core.data.Data {
    @Element
    protected int brightness;
    
    @Element
    protected int white;
    
    @Element
    protected int temp;
    
    @Element
    protected String turn;
  }
  
  public static class StatusData extends Data {
    @Element
    protected boolean ison;
    
    @Element
    protected boolean has_timer;
    
    @Element
    protected int timer_startet;
    
    @Element
    protected int timer_duration;
    
    @Element
    protected int timer_remaining;
  }
  
  public void setBrightness(int value) throws CoreException {
    this.data.brightness = value;
    Data data0 = createData();
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
  }
  
  public void setColorTemp(int value) throws CoreException {
    Data data0 = createData();
    data0.temp = value;
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
  }
  
  public Switch.State toggle() throws CoreException {
    Data data0 = createData();
    if (this.state == Switch.State.ON) {
      this.state = Switch.State.OFF;
    } else {
      this.state = Switch.State.ON;
    } 
    data0.turn = mapState(this.state);
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
    return this.state;
  }
  
  public Switch.State setState(Switch.State state) throws CoreException {
    this.state = state;
    Data data0 = createData();
    data0.turn = mapState(state);
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
    return this.state;
  }
  
  public Data createData() {
    Data data0 = new Data();
    data0.brightness = this.data.brightness;
    data0.temp = this.data.temp;
    data0.white = 0;
    data0.turn = this.data.ison ? "on" : "off";
    return data0;
  }
  
  protected void initSubscriber() {
    this.subscriber = new MqttSubscriber("shellies/" + this.shellyId + "/light/0/status") {
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          if (topic.endsWith("/light/0/status")) {
            ShellyDuo.this.data = (ShellyDuo.StatusData)Coding.decode(message.getPayload(), "json", ShellyDuo.StatusData.class);
            ShellyDuo.this.state = ShellyDuo.this.data.ison ? Switch.State.ON : Switch.State.OFF;
            ShellyDuo.this.export();
          } 
        }
      };
    try {
      this.mqttClient.subscribe(this.subscriber);
    } catch (CoreException coreException) {}
  }
  
  public void finish() {
    this.stateMap = new HashMap<>();
    this.stateMap.put("on", Switch.State.ON.toString());
    this.stateMap.put("off", Switch.State.OFF.toString());
    publishTopic="shellies/" + this.shellyId + "/light/0/set";
  }
  
  public ExportData createExportData() {
    return new ExportData(getDeviceHandle(), name, this.data);
  }
  
  public boolean canDim() throws CoreException {
    return true;
  }
  
  public boolean canColorTemp() throws CoreException {
    return true;
  }
  
  public boolean canColor() throws CoreException {
    return false;
  }
  
  public int getBrightness() throws CoreException {
    return this.data.brightness;
  }
  
  public int getColorTemp() throws CoreException {
    return this.data.temp;
  }
  
  public List<Light.Color> getColorPresets() throws CoreException {
    return null;
  }
  
  public void setColor(Light.Color color) throws CoreException {
    CoreException.throwCoreException("setColor is not Supported");
  }
  
  public Light.Color getColor() throws CoreException {
    return null;
  }
}
