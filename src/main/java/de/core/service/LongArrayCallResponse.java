package de.core.service;

import de.core.serialize.annotation.Element;

public class LongArrayCallResponse implements CallResponse {
  @Element
  public long[] value;
  
  protected LongArrayCallResponse() {}
  
  public LongArrayCallResponse(long[] value) {
    this.value = value;
  }
  
  public long[] getValue() {
    return this.value;
  }
}
