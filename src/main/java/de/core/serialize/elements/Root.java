package de.core.serialize.elements;

import java.util.ArrayList;
import java.util.List;

public class Root extends ComplexElement {
  List<Entity> entities;
  
  public Root() {
    super(null);
  }
  
  public void addEntity(Entity entity) {
    if (this.entities == null)
      this.entities = new ArrayList<>(); 
    int idx = this.entities.indexOf(entity);
    if (idx == -1) {
      this.entities.add(entity);
    } else {
      this.entities.set(idx, entity);
    } 
  }
  
  public Entity getEntity(String key) {
    for (Entity entity : this.entities) {
      if (entity.getName().equals(key))
        return entity; 
    } 
    return null;
  }
  
  public Root getRoot() {
    return this;
  }
}
