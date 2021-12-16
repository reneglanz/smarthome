package de.core.server;

import de.core.CoreException;
import de.core.Env;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.HashMap;
import java.util.Map;

public class Setup implements Serializable {
  @Element
  protected Map<String, String> data = new HashMap<>();
  
  public void finish() {
    for (Map.Entry<String, String> entry : this.data.entrySet()) {
      try {
        Env.put(entry.getKey(), entry.getValue());
      } catch (CoreException coreException) {}
    } 
  }
}
