package de.core.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Cache<KEY, OBJ> {
  protected Map<KEY, OBJ> data = Collections.synchronizedMap(new HashMap<>());
  
  public void put(KEY key, OBJ obj) {
    this.data.put(key, obj);
  }
  
  public void invalidate(KEY key) {
    this.data.remove(key);
  }
  
  public void invalidateAll() {
    this.data.clear();
  }
  
  public OBJ get(KEY key) {
    return this.data.get(key);
  }
}
