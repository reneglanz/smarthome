package de.core.service;

import de.core.serialize.annotation.Element;

public class ExceptionResponse implements CallResponse {
  @Element
  String message;
  
  @Element
  int code;
  
  @Element
  String trace;
  
  @Element
  String source;
  
  protected ExceptionResponse() {}
  
  public ExceptionResponse(String message, String source, int code, String trace) {
    this.message = message;
    this.code = code;
    this.trace = trace;
  }
  
  public ExceptionResponse(String message, String source, String trace) {
    this(message, source, -1, trace);
  }
  
  public String getMessage() {
    return this.message;
  }
  
  public int getCode() {
    return this.code;
  }
  
  public String getTrace() {
    return this.trace;
  }
  
  public Object getValue() {
    return this;
  }
  
  public String getSource() {
    return this.source;
  }
}
