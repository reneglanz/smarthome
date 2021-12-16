package de.core.http.handler;

import de.core.http.HttpRequest;
import de.core.http.HttpResponse;

public class TestRequestHandler extends AbstractHttpRequestHandler {
  public HttpResponse handleRequest(HttpRequest request) {
    int read = 0;
    byte[] ba = new byte[1024];
    int sum = 0;
    try {
      while ((read = request.getIs().read(ba)) != -1)
        sum += read; 
    } catch (Throwable t) {
      t.getStackTrace();
    } 
    System.out.println(sum);
    return new FixedLengthHttpResponse("OK".getBytes(), 200);
  }
  
  public boolean keepAlive() {
    return false;
  }
}
