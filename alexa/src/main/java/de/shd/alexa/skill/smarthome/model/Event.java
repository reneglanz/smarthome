package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Event implements Serializable {
  @Element
  protected Header header;
  
  @Element
  protected Endpoint endpoint;
  
  @Element
  protected EventPayload payload;
  
  public Event(Header header, EventPayload payload) {
    this.header = header;
    this.payload = payload;
  }
  
  public Event(Header header, Endpoint endpoint) {
    this.header = header;
    this.endpoint = endpoint;
  }
  
  public EventPayload getPayload() {
    return this.payload;
  }
  
  public void setPayload(EventPayload payload) {
    this.payload = payload;
  }
  
  public Header getHeader() {
    return this.header;
  }
  
  public void setHeader(Header header) {
    this.header = header;
  }
}
