package de.shd.alexa.skill.custom.handler;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.service.Services;
import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;
import de.shd.device.Switch;

public class SwitchIntentHandler implements IntentHandler {
  protected NameHandle provider;
  
  public SwitchIntentHandler(NameHandle provider) {
    this.provider = provider;
  }
  
  public AlexaResponse handle(AlexaRequest request) throws CoreException {
    String deviceName = request.getSlot("device", null);
    if (deviceName != null) {
      Switch switch0 = (Switch)Services.get((Handle)this.provider, (Handle)new NameHandle(deviceName), Switch.class);
      if (switch0 != null)
        switch0.setState("SwitchSetOn".equals(request.getIntentName()) ? Switch.State.ON : Switch.State.OFF); 
      return (new AlexaResponse()).speak("Ok");
    } 
    return null;
  }
  
  public boolean canHandle(AlexaRequest request) {
    return ("SwitchSetOn".equals(request.getIntentName()) || "SwitchSetOff".equals(request.getIntentName()));
  }
}
