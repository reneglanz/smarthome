package de.core.io;

import de.core.CoreException;
import de.core.rt.Scope;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataHandle {
  InputStream getData() throws CoreException;
  
  void saveData(OutputStream paramOutputStream) throws CoreException;
  
  static DataHandle create() {
    if (Scope.getScope() == Scope.LOCAL)
      return new LocalDataHandle(); 
    return new RemoteDataHandle();
  }
}
