package de.core.service;

import de.core.serialize.annotation.Element;

public class ObjectCallResponse implements CallResponse {
  @Element
  public Object value;
  
  protected ObjectCallResponse() {}
  
  public ObjectCallResponse(Object value) {
    this.value = value;
  }
  
  public Object getValue() {
    return this.value;
  }
}
