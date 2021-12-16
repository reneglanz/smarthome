package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Response implements Serializable {
  @Element
  protected Event event;
  
  @Element
  protected Context context;
  
  protected Response() {}
  
  public Response(Event event) {
    this.event = event;
  }
  
  public Response(Event event, Context context) {
    this.event = event;
    this.context = context;
  }
  
  public Event getEvent() {
    return this.event;
  }
  
  public void setEvent(Event event) {
    this.event = event;
  }
}
