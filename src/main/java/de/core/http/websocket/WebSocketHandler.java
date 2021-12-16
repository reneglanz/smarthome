package de.core.http.websocket;

public interface WebSocketHandler {
  void onMessage(WebSocketFrame paramWebSocketFrame);
  
  void onOpen(WebSocket paramWebSocket);
  
  void onClose();
}
