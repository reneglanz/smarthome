package de.core.service;

import de.core.serialize.Serializable;

public interface Service extends Serializable {
  default String getServiceHandle() {
    return getClass().getName();
  }
  
  default boolean implements0(Class<?> clazz) {
    return clazz.isAssignableFrom(getClass());
  }
}
