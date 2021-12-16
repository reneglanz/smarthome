package de.core.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class HttpResponse {
  protected int statusCode = 0;
  
  List<HttpHeader> header;
  
  public HttpResponse() {
    this.header = new ArrayList<>();
  }
  
  public void addHeader(HttpHeader header) {
    this.header.add(header);
  }
  
  public int getStatusCode() {
    return this.statusCode;
  }
  
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
  
  public List<HttpHeader> getHeader() {
    return this.header;
  }
  
  public void setContentType(String type) {
    setHeader(new HttpHeader("Content-Type", type));
  }
  
  public void setFilename(String filename) {
    setHeader(new HttpHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""));
  }
  
  public void setHeader(HttpHeader _header) {
    if (_header.getName() == null)
      return; 
    boolean found = false;
    for (HttpHeader h : this.header) {
      if (_header.getName().equals(h.getName())) {
        h.setValue(_header.getValue());
        found = true;
        break;
      } 
    } 
    if (!found)
      addHeader(_header); 
  }
  
  public HttpHeader getHeader(String name) {
	  for(HttpHeader h:header) {
		  if(h.getName()!=null&&h.getName().equalsIgnoreCase(name)) {
			  return h;
		  }
	  }
	  return null;
  }
  
  public boolean removeHeader(String name) {
	  HttpHeader toRemove=getHeader(name);
	  if(toRemove!=null) {
		  header.remove(toRemove);
		  return true;
	  }
	  return false;
  }
  
  public abstract int getContentLength();
  
  public abstract InputStream getContent();
  
  public abstract void prepareForSend();
}
