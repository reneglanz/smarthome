package de.shd.alexa.skill.custom.handler;

import de.core.CoreException;
import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;

public class AutomationHandler implements IntentHandler {
  public AlexaResponse handle(AlexaRequest request) throws CoreException {
    return null;
  }
  
  public boolean canHandle(AlexaRequest request) {
    return false;
  }
}
