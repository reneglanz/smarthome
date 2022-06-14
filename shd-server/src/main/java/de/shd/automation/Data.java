package de.shd.automation;

import de.core.log.Logger;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.HashMap;

public class Data implements Serializable {
  @Element
  protected HashMap<String, Object> data = new HashMap<>();
  
  @SuppressWarnings("unchecked")
  public <E> E get(String key, Class<E> clazz, E default0) {
    Object o = this.data.get(key);
    if (o != null && clazz.isAssignableFrom(o.getClass()))
      return (E)o; 
    return default0;
  }
  
  public void set(String key, Object value) {
    this.data.put(key, value);
  }
  
  public HashMap<String, Object> getData() {
    return this.data;
  }
  
  public Logger getLogger() {
	  return Logger.createLogger("AutomationData");
  }
}
