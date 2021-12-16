package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;

public interface Properties extends Serializable {
  public static class Factory {
    public static Properties powerController(String value) {
      return new ControllerProperties("Alexa.PowerController", "powerState", value);
    }
    
    public static Properties colorTempController(int value) {
      return new ControllerProperties("Alexa.ColorTemperatureController", "colorTemperatureInKelvin", "" + value);
    }
    
    public static Properties brigthnessController(int value) {
      return new ControllerProperties("Alexa.BrightnessController", "brightness", "" + value);
    }
    
    public static Properties colorController(ColorHSB color) {
      return new ColorControllerProperties("Alexa.ColorController", "color", color);
    }
    
    public static Properties rollerRangeController(int range) {
    	return new RollerRangeControllerProperties(""+range);
    }
  }
}
