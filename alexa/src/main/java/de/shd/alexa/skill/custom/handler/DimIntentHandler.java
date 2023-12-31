package de.shd.alexa.skill.custom.handler;

import de.core.CoreException;
import de.core.service.Services;
import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;
import de.shd.device.Light;
import de.shd.device.State;

public class DimIntentHandler implements IntentHandler {
  protected String provider;
  
  public DimIntentHandler(String provider) {
    this.provider = provider;
  }
  
  public AlexaResponse handle(AlexaRequest request) throws CoreException {
    String deviceName = request.getSlot("device", null);
    String dimValue = request.getSlot("dimValue", null);
    if (deviceName != null) {
      Light light = (Light)Services.get(this.provider, deviceName, Light.class);
      if (light != null) {
        if (light.canDim()) {
          int brightness = light.getBrightness();
          int dimValueInt = (dimValue != null) ? Integer.parseInt(dimValue) : 20;
          if (brightness - dimValueInt > 0) {
            light.setBrightness(brightness - dimValueInt);
          } else {
            light.setState(State.OFF);
          } 
          return (new AlexaResponse()).speak("Ok");
        } 
        return (new AlexaResponse()).speak("Das Geräte " + deviceName + " kann nicht gedimmt werden");
      } 
      return (new AlexaResponse()).speak("Ich konnte kein Gerät mit den Name " + deviceName + " finden");
    } 
    return (new AlexaResponse()).speak("Ich habe nicht verstanden");
  }
  
  public boolean canHandle(AlexaRequest request) {
    return "DimIntent".equals(request.getIntentName());
  }
}
