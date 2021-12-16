package de.core.serialize.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComplexElement extends Element {
  private List<Element> children = new ArrayList<>();
  
  protected String type;
  
  public ComplexElement(Element parent) {
    super(parent);
  }
  
  public ComplexElement(Element parent, String name) {
    super(parent);
    this.name = name;
  }
  
  public void add(Element child) {
    if (child != this)
      getChildren().add(child); 
  }
  
  public void forEach(Consumer<Element> consumer) {
    for (Element c : getChildren())
      consumer.accept(c); 
  }
  
  public Element getChild(String name) {
    for (Element child : getChildren()) {
      if (child.getName().equals(name))
        return child; 
    } 
    return null;
  }
  
  public int getSize() {
    return getChildren().size();
  }
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public Root getRoot() {
    return getParent().getRoot();
  }
  
  public List<Element> getChildren() {
    return this.children;
  }
  
  public void setChildren(List<Element> children) {
    this.children = children;
  }
}
