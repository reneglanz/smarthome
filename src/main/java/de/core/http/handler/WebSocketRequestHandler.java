package de.core.http.handler;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.websocket.WebSocket;
import de.core.http.websocket.WebSocketManager;
import de.core.serialize.annotation.Injectable;
import java.io.IOException;

public class WebSocketRequestHandler extends AbstractHttpRequestHandler {
  @Injectable(selfInjecting = true)
  protected WebSocketManager manager;
  
  public HttpResponse handleRequest(HttpRequest request) {
    WebSocket websocket = this.manager.createWebSocket(request.getThread().getSocket());
    HttpResponse response = websocket.handshake(request);
    response.prepareForSend();
    try {
      request.getThread().writeResponse(response);
      this.manager.fireOnOpen(request.getRequestPath(), websocket);
      websocket.loop();
    } catch (IOException iOException) {}
    return null;
  }
  
  public boolean canHandleRequest(HttpRequest request) {
    HttpHeader upgradeHeader = request.getHeader("Upgrade");
    if (request.getMethod().equals("GET") && upgradeHeader != null && upgradeHeader
      
      .getValue().equals("websocket"))
      return true; 
    return false;
  }
  
  public boolean keepAlive() {
    return true;
  }
}
