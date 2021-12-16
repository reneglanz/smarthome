package de.core.service;

import de.core.serialize.annotation.Element;

public class CharArrayCallResponse implements CallResponse {
  @Element
  public char[] value;
  
  protected CharArrayCallResponse() {}
  
  public CharArrayCallResponse(char[] value) {
    this.value = value;
  }
  
  public char[] getValue() {
    return this.value;
  }
}
