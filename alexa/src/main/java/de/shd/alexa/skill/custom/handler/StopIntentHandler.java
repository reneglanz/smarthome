package de.shd.alexa.skill.custom.handler;

import de.core.CoreException;
import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;

public class StopIntentHandler implements IntentHandler {
  public AlexaResponse handle(AlexaRequest request) throws CoreException {
    return (new AlexaResponse()).speak("Over").endSession();
  }
  
  public boolean canHandle(AlexaRequest request) {
    return "AMAZON.StopIntent".equals(request.getIntentName());
  }
}
