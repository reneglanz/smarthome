package de.core.http.websocket;

import de.core.CoreException;

public class WebSocketException extends CoreException {
  private static final long serialVersionUID = -9187189165001883145L;
  
  public boolean closeConnection = false;
  
  public WebSocketException(Throwable t, boolean closeConnection) {
    super(t);
    this.closeConnection = closeConnection;
  }
  
  public WebSocketException(String msg, boolean closeConnection) {
    super(msg);
    this.closeConnection = closeConnection;
  }
  
  public boolean isCloseConnection() {
    return this.closeConnection;
  }
  
  public void setCloseConnection(boolean closeConnection) {
    this.closeConnection = closeConnection;
  }
}
