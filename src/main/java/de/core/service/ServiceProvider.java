package de.core.service;

import java.util.List;

import de.core.CoreException;

public interface ServiceProvider<E extends Service> {
  E getService(String paramHandle) throws CoreException;
  
  E getService(Class<E> paramClass) throws CoreException;
  
  void bind(E paramE) throws CoreException;
  
  void unbind(E paramE) throws CoreException;
  
  void unbind(String handle) throws CoreException;
  
  String getProviderId();
  
  List<String> getServiceIds();
}
