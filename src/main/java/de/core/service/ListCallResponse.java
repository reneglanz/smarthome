package de.core.service;

import de.core.serialize.annotation.Element;
import java.util.List;

public class ListCallResponse implements CallResponse {
  @Element
  public List<?> value;
  
  protected ListCallResponse() {}
  
  public ListCallResponse(List<?> value) {
    this.value = value;
  }
  
  public Object getValue() {
    return this.value;
  }
}
