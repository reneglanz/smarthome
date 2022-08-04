package de.shd.ui;

import de.core.CoreException;
import de.core.http.websocket.WebSocket;
import de.core.http.websocket.WebSocketFrame;
import de.core.http.websocket.WebSocketHandler;
import de.core.http.websocket.WebSocketManager;
import de.core.log.Logger;
import de.core.serialize.Coding;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.shd.device.AbstractDevice;
import de.shd.device.DeviceProvider;
import de.shd.device.ExportData;
import de.shd.update.UpdateService;

public class UiUpdateListener implements UpdateService.UpdateListener, WebSocketHandler {
  @Injectable(selfInjecting = true) protected WebSocketManager manager;
  @Injectable(selfInjecting = true) protected DeviceProvider store;
  @Element(defaultValue = "/updates") protected String path;
  
  Logger logger=Logger.createLogger("UiUpdateListener");
  
  public void onUpdate(ExportData data) throws CoreException {
    try {
		this.manager.broadcast(this.path, Coding.encode(data, "json"));
	} catch (CoreException e) {
		CoreException.throwCoreException(e);
	}
  }
  
  public void onMessage(WebSocketFrame frame) {}
  
  public void onOpen(WebSocket websocket) {
      for (AbstractDevice w : this.store) { 
    	  try {
	        w.export(); 
	      } catch (Throwable t) {
	    	  logger.error("Failed to export Data for "+w.getDeviceId() + " to websocket");
	      }
      }
  }
  
  public void onClose() {}
  
  public void finish() {
    this.manager.registerHandler(this.path, this);
  }
}
