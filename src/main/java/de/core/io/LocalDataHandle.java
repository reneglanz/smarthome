package de.core.io;

import de.core.CoreException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalDataHandle implements DataHandle {
  public InputStream getData() throws CoreException {
    return null;
  }
  
  public void saveData(OutputStream os) throws CoreException {}
}
