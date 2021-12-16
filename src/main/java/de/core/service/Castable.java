package de.core.service;

import de.core.CoreException;

public interface Castable {
  default <T> T castTo(Class<T> clazz) throws CoreException {
    if (clazz.isAssignableFrom(getClass()))
      return (T)this; 
    CoreException.throwCoreException(getClass().toString() + " does not match expected type " + clazz);
    return null;
  }
}
