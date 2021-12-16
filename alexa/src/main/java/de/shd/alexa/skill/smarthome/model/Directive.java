package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Directive implements Serializable {
  @Element
  protected Header header;
  
  @Element
  protected Endpoint endpoint;
  
  @Element
  protected Payload payload;
  
  public Header getHeader() {
    return this.header;
  }
  
  public Payload getPayload() {
    return this.payload;
  }
  
  public Endpoint getEndpoint() {
    return this.endpoint;
  }
}
