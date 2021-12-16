package de.core.service;

import de.core.serialize.annotation.Element;

public class IntArrayCallResponse implements CallResponse {
  @Element
  public int[] value;
  
  protected IntArrayCallResponse() {}
  
  public IntArrayCallResponse(int[] value) {
    this.value = value;
  }
  
  public int[] getValue() {
    return this.value;
  }
}
