package de.shd.alexa.skill.custom.handler;

import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;

public class LaunchIntentHandler implements IntentHandler {
  public AlexaResponse handle(AlexaRequest request) {
    return (new AlexaResponse()).speak("Was kann ich für dich tun?");
  }
  
  public boolean canHandle(AlexaRequest request) {
    return "LaunchRequest".equals(request.getIntentType());
  }
}
