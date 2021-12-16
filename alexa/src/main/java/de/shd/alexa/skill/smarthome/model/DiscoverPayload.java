package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.annotation.Element;

public class DiscoverPayload implements EventPayload {
  @Element
  protected Endpoint[] endpoints;
  
  protected DiscoverPayload() {}
  
  public DiscoverPayload(Endpoint[] endpoints) {
    this.endpoints = endpoints;
  }
}
