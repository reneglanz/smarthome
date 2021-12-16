package de.core.log;

import de.core.serialize.Serializable;

public interface Appender extends Serializable {
  void write(String paramString);
}
