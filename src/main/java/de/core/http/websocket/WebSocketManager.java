package de.core.http.websocket;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Injectable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Injectable(selfInjecting = true)
public class WebSocketManager implements Serializable {
  List<WebSocket> webSockets = new ArrayList<>();
  
  Map<String, List<WebSocketHandler>> handler = new HashMap<>();
  
  public WebSocket createWebSocket(Socket socket) {
    WebSocket webSocket = new WebSocket(socket, this);
    this.webSockets.add(webSocket);
    return webSocket;
  }
  
  public void removeWebSocket(WebSocket webSocket) {
    this.webSockets.remove(webSocket);
  }
  
  public synchronized void registerHandler(String path, WebSocketHandler handler) {
    List<WebSocketHandler> list = this.handler.get(path);
    if (list == null) {
      list = new ArrayList<>();
      this.handler.put(path, list);
    } 
    list.add(handler);
  }
  
  public void fireOnMessageReceived(String path, WebSocketFrame frame) {
    List<WebSocketHandler> list = this.handler.get(path);
    if (list != null)
      for (WebSocketHandler handler : list)
        handler.onMessage(frame);  
  }
  
  public void fireOnOpen(String path, WebSocket websocket) {
    List<WebSocketHandler> list = this.handler.get(path);
    if (list != null)
      for (WebSocketHandler handler : list)
        handler.onOpen(websocket);  
  }
  
  public void fireOnClose(String path, WebSocketFrame frame) {
    List<WebSocketHandler> list = this.handler.get(path);
    if (list != null)
      for (WebSocketHandler handler : list)
        handler.onMessage(frame);  
  }
  
  public void broadcast(String path, String data) throws CoreException {
	  this.broadcast(path, data.getBytes());
  }
  
  public void broadcast(String path, byte[] data) throws CoreException {
    WebSocketFrame frame = new WebSocketFrame(WebSocketFrame.Opcode.TEXT, data);
    List<WebSocket> errorSockets = new ArrayList<>();
    for (WebSocket ws : this.webSockets) {
      try {
        ws.send(frame);
      } catch (IOException e) {
        errorSockets.add(ws);
      } 
    } 
    errorSockets.forEach(ws -> {
          try {
            ws.close();
          } catch (Exception exception) {}
        });
  }
}
