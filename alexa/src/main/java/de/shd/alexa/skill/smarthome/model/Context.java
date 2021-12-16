package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Context implements Serializable {
  @Element
  protected Properties[] properties;
  
  public Context(Properties... properties) {
    this.properties = properties;
  }
  
  public Context(Properties properties) {
    this.properties = new Properties[] { properties };
  }
  
  protected Context() {}
}
