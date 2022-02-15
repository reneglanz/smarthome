package de.shd.alexa.skill.custom.handler;

import de.core.CoreException;
import de.core.service.Services;
import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;
import de.shd.device.Light;

public class SetBrightnessIntentHandler implements IntentHandler {
  protected String provider;
  
  public SetBrightnessIntentHandler(String provider) {
    this.provider = provider;
  }
  
  public AlexaResponse handle(AlexaRequest request) throws CoreException {
    String deviceName = request.getSlot("device", null);
    String value = request.getSlot("brightness", null);
    if (deviceName != null) {
      Light light = (Light)Services.get(this.provider, deviceName, Light.class);
      if (light != null) {
        if (light.canDim()) {
          int dim = 30;
          if (value != null) {
            dim = Integer.parseInt(value);
            light.setBrightness(dim);
            return (new AlexaResponse()).speak("Ok");
          } 
        } else {
          return (new AlexaResponse()).speak("Das Geräte " + deviceName + " kann nicht gedimmt werden");
        } 
      } else {
        return (new AlexaResponse()).speak("Ich konnte kein Gerät mit den Name " + deviceName + " finden");
      } 
    } else {
      (new AlexaResponse()).speak("Welches Gerät soll gedimmt werden?");
    } 
    return (new AlexaResponse()).speak("Ich habe nicht verstanden");
  }
  
  public boolean canHandle(AlexaRequest request) {
    return "SetBrightness".equals(request.getIntentName());
  }
}
