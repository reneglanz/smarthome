package de.shd.automation;

import de.core.log.Logger;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.HashMap;

public class Data implements Serializable {
  
  @Element protected HashMap<String,Object>values=new HashMap<>();
	
  @SuppressWarnings("unchecked")
  public <E> E get(String key, Class<E> clazz, E default0) {
    Object o = this.values.get(key);
    if (o != null && clazz.isAssignableFrom(o.getClass()))
      return (E)o; 
    return default0;
  }
  
  public void set(String key, Object value) {
    this.values.put(key, value);
  }
  
  public Logger getLogger() {
	  return Logger.createLogger("AutomationData");
  }
  
  public void merge(Data other) {
	  this.values.putAll(other.values);
  }
}
