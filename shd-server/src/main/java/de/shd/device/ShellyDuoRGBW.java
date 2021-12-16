package de.shd.device;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.core.CoreException;
import de.core.Env;
import de.core.data.Data;
import de.core.mqtt.MqttSubscriber;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.device.data.BrightnessData;
import de.shd.device.data.SwitchData;

public class ShellyDuoRGBW extends MqttSwitchDevice implements Light {
  public static final String COLOR_MODE = "color";
  
  public static final String WHITE_MODE = "white";
  
  @Element
  String shellyId;
  
  protected ColorPresets colors;
  
  ShellyStatusData data;
  
  public static class ShellyStatusData implements Cloneable, Serializable {
    @Element
    protected String turn;
    
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
    
    @Element
    protected String mode;
    
    @Element
    protected int red;
    
    @Element
    protected int green;
    
    @Element
    protected int blue;
    
    @Element
    protected int white;
    
    @Element
    protected int gain;
    
    @Element
    protected int temp;
    
    @Element
    protected int brightness;
    
    @Element
    protected int effect;
    
    protected Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  }
  
  public boolean canDim() throws CoreException {
    return true;
  }
  
  public boolean canColorTemp() throws CoreException {
    return true;
  }
  
  public boolean canColor() throws CoreException {
    return true;
  }
  
  public List<Light.Color> getColorPresets() throws CoreException {
    if (this.colors == null) {
      this.colors = (ColorPresets)Env.get(ColorPresets.class);
      if (this.colors == null)
        this.colors = new ColorPresets(); 
    } 
    return this.colors.get();
  }
  
  protected void initSubscriber() {
    this.subscriber = new MqttSubscriber("shellies/" + this.shellyId + "/color/0/status") {
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          if (topic.endsWith("/color/0/status")) {
            ShellyDuoRGBW.this.data = (ShellyDuoRGBW.ShellyStatusData)Coding.decode(message.getPayload(), "json", ShellyDuoRGBW.ShellyStatusData.class);
            ShellyDuoRGBW.this.state = ShellyDuoRGBW.this.data.ison ? Switch.State.ON : Switch.State.OFF;
            ShellyDuoRGBW.this.export();
          } 
        }
      };
    try {
      this.mqttClient.subscribe(this.subscriber);
    } catch (CoreException coreException) {}
  }
  
  public void finish() {
	  publishTopic="shellies/" + this.shellyId + "/color/0/set";
  }
  
  public ShellyStatusData createData() {
    try {
    	if(this.data==null) {
    		this.data=new ShellyStatusData();
    	}
    	return (ShellyStatusData)this.data.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public Switch.State toggle() throws CoreException {
    ShellyStatusData data0 = createData();
    if (this.state == Switch.State.ON) {
      this.state = Switch.State.OFF;
    } else {
      this.state = Switch.State.ON;
    } 
    data0.turn = this.state.toString().toLowerCase();
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
    return this.state;
  }
  
  public Switch.State setState(Switch.State state) throws CoreException {
    this.state = state;
    ShellyStatusData data0 = createData();
    data0.turn = state.toString().toLowerCase();
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
    return this.state;
  }
  
  public void setBrightness(int value) throws CoreException {
    ShellyStatusData data0 = createData();
    data0.gain = value;
    data0.brightness = value;
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
  }
  
  public void setColorTemp(int value) throws CoreException {
    ShellyStatusData data0 = createData();
    data0.temp = value;
    data0.mode = "white";
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
  }
  
  public void setColor(Light.Color color) throws CoreException {
    ShellyStatusData data0 = createData();
    data0.mode = "color";
    data0.red = color.r;
    data0.green = color.g;
    data0.blue = color.b;
    mqttPublish(publishTopic,new String(Coding.encode(data0, "json")));
  }
  
  public int getBrightness() throws CoreException {
    return this.data.brightness;
  }
  
  public int getColorTemp() throws CoreException {
    return this.data.temp;
  }
  
  public ExportData createExportData() {
    return new ExportData(getDeviceHandle(), name,new SwitchData(this.state), new BrightnessData(this.data.brightness));
  }
  
  public Light.Color getColor() throws CoreException {
    return new Light.Color(this.data.red, this.data.green, this.data.blue);
  }
}
