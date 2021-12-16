package de.core.service;

import de.core.CoreException;
import de.core.handle.Handle;

public interface ServiceProvider<E extends Service> {
  E getService(Handle paramHandle) throws CoreException;
  
  E getService(Class<E> paramClass) throws CoreException;
  
  void bind(E paramE) throws CoreException;
  
  void unbind(E paramE) throws CoreException;
  
  void unbind(Handle handle) throws CoreException;
  
  Handle getProviderId();
}
