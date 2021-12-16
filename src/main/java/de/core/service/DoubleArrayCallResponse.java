package de.core.service;

import de.core.serialize.annotation.Element;

public class DoubleArrayCallResponse implements CallResponse {
  @Element
  public double[] value;
  
  protected DoubleArrayCallResponse() {}
  
  public DoubleArrayCallResponse(double[] value) {
    this.value = value;
  }
  
  public double[] getValue() {
    return this.value;
  }
}
