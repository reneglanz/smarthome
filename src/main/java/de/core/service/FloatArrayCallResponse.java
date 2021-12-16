package de.core.service;

import de.core.serialize.annotation.Element;

public class FloatArrayCallResponse implements CallResponse {
  @Element
  public float[] value;
  
  protected FloatArrayCallResponse() {}
  
  public FloatArrayCallResponse(float[] value) {
    this.value = value;
  }
  
  public float[] getValue() {
    return this.value;
  }
}
