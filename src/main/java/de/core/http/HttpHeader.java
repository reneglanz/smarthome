package de.core.http;

public class HttpHeader {
  public static final String TRANSFER_ENCODING = "Transfer-Encoding";
  
  public static final String TRANSFER_ENCODING_CHUNKED = "chunked";
  
  public static final String CONTENT_TYPE = "Content-Type";
  
  public static final String CONTENT_DISPOSITION = "Content-Disposition";
  
  public static final String CONTENT_LENGTH = "Content-Length";
  
  public static final String AUTH_TOKEN = "auth-token";
  
  protected String name;
  
  protected String value;
  
  protected String parameter;
  
  protected HttpHeader() {}
  
  public HttpHeader(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getValue() {
    return this.value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public String getParameter() {
    return this.parameter;
  }
  
  public void setParameter(String parameter) {
    this.parameter = parameter;
  }
  
  public String toString() {
    return "HttpHeader [name=" + this.name + ", value=" + this.value + ", parameter=" + this.parameter + "]";
  }
}
