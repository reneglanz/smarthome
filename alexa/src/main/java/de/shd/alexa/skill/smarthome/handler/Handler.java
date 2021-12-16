package de.shd.alexa.skill.smarthome.handler;

import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;

public interface Handler {
  Response handle(Request paramRequest);
}
