package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Scope implements Serializable {
  @Element
  protected String type;
  
  @Element
  protected String token;
}
