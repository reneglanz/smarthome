package de.core.serialize.elements;

public class Entity extends Element {
  Object entityValue;
  
  public Entity(Root root) {
    super(root);
  }
  
  public Object getValue() {
    return this.entityValue;
  }
  
  public void setValueObject(Object o) {
    this.entityValue = o;
  }
  
  public Root getRoot() {
    return getParent().getRoot();
  }
}
