package de.shd.alexa.skill.custom.handler;

import de.core.CoreException;
import de.shd.alexa.skill.custom.AlexaRequest;
import de.shd.alexa.skill.custom.AlexaResponse;

public interface IntentHandler {
  AlexaResponse handle(AlexaRequest paramAlexaRequest) throws CoreException;
  
  boolean canHandle(AlexaRequest paramAlexaRequest);
}
