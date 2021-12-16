package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Request implements Serializable {
  @Element
  protected Directive directive;
  
  public Directive getDirective() {
    return this.directive;
  }
}
