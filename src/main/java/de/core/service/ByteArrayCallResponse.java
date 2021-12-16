package de.core.service;

import de.core.serialize.annotation.Element;

public class ByteArrayCallResponse implements CallResponse {
  @Element
  public byte[] value;
  
  protected ByteArrayCallResponse() {}
  
  public ByteArrayCallResponse(byte[] value) {
    this.value = value;
  }
  
  public byte[] getValue() {
    return this.value;
  }
}
