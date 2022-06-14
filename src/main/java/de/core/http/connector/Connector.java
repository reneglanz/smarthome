package de.core.http.connector;

import de.core.CoreException;
import de.core.http.HttpServer;
import de.core.serialize.Serializable;
import java.net.ServerSocket;

public interface Connector extends Runnable, Serializable {
  void stop();
  
  void init(HttpServer paramHttpServer);
  
  String getName();
  
  ServerSocket createSocket() throws CoreException;
  
  int getPort();
}
