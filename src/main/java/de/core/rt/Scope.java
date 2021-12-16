package de.core.rt;

import de.core.CoreException;

public enum Scope {
  LOCAL, REMOTE;
  
  private static Object SYNC;
  
  private static String serviceHost;
  
  private static int servicePort;
  
  static {
    SYNC = new Object();
    servicePort = 7000;
  }
  
  public static Scope getScope() {
    return (serviceHost == null) ? LOCAL : REMOTE;
  }
  
  public static void setHost(String host) {
    synchronized (SYNC) {
      int index = host.indexOf(":");
      serviceHost = (index > 0) ? host.substring(0, index) : host;
      servicePort = (index > 0) ? Integer.parseInt(host.substring(index + 1, host.length())) : 7000;
    } 
  }
  
  public static String getServiceHost() throws CoreException {
    if (serviceHost == null)
      CoreException.throwCoreException("Service Host not set"); 
    return serviceHost;
  }
  
  public static int getServicePort() {
    return servicePort;
  }
}
