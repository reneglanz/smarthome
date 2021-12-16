package de.core.rt;

import de.core.serialize.Serializable;

public interface Resource extends Serializable {
  default int getPriority() {
    return 10;
  }
  
  default Object getName() {
    return getClass();
  }
}
