package de.core.http.handler;

import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.HttpServer;

public interface HttpRequestHandler {
  HttpResponse handleRequest(HttpRequest paramHttpRequest);
  
  boolean canHandleRequest(HttpRequest paramHttpRequest);
  
  boolean keepAlive();
  
  default void registerHttpRequestHandler() {
    HttpServer.registerHttpRequestHandler(this);
  }
}
