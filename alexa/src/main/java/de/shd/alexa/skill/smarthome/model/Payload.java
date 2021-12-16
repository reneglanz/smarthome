package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Payload implements Serializable {
  @Element protected Scope scope;
  @Element protected int brightness;
  @Element protected int colorTemperatureInKelvin;
  @Element protected ColorHSB color;
  @Element protected int rangeValue;
  
  public Scope getScope() {
    return this.scope;
  }
  
  public void setScope(Scope scope) {
    this.scope = scope;
  }
  
  public int getBrightness() {
    return this.brightness;
  }
  
  public void setBrightness(int brightness) {
    this.brightness = brightness;
  }
  
  public int getColorTemperatureInKelvin() {
    return this.colorTemperatureInKelvin;
  }
  
  public void setColorTemperatureInKelvin(int colorTemperatureInKelvin) {
    this.colorTemperatureInKelvin = colorTemperatureInKelvin;
  }
  
  public ColorHSB getColorHSB() {
    return this.color;
  }
  
  public void setColorHSB(ColorHSB color) {
    this.color = color;
  }

  public int getRangeValue() {
	return rangeValue;
  }
}
