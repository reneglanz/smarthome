package de.core;

import de.core.service.ExceptionResponse;

public class CoreException extends Exception {
  private static final long serialVersionUID = 3629575150149510946L;
  
  public static CoreException throwCoreException(Throwable t) throws CoreException {
    if (t instanceof CoreException)
      throw (CoreException)t; 
    CoreException e=new CoreException(t);
    throw e;
  }
  
  public static <T> T throwCoreException(String message) throws CoreException {
    throw new CoreException(message);
  }
  
  public CoreException() {}
  
  public CoreException(String message) {
    super(message);
  }
  
  protected CoreException(Throwable cause) {
    super(cause);
  }
  
  public CoreException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public CoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
  
  public static void throwCoreException(ExceptionResponse obj) throws CoreException {
    throw new CoreException(obj.getMessage() + "\n" + ((obj.getSource() != null) ? (obj.getSource() + "@") : "") + obj.getTrace());
  }
  
}
