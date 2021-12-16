package de.core.service;

import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.serialize.Serializable;

public interface Service extends Serializable {
  default Handle getServiceHandle() {
    return (Handle)new NameHandle(getClass().getName());
  }
  
  default boolean implements0(Class<?> clazz) {
    return clazz.isAssignableFrom(getClass());
  }
}
