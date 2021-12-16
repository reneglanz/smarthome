package de.core.serialize.elements;

public abstract class Element {
  String name;
  
  Element parent;
  
  public Element(Element parent) {
    this.parent = parent;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Element getParent() {
    return this.parent;
  }
  
  public void setParent(Element parent) {
    this.parent = parent;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    Element other = (Element)obj;
    if (this.name == null) {
      if (other.name != null)
        return false; 
    } else if (!this.name.equals(other.name)) {
      return false;
    } 
    return true;
  }
  
  public abstract Root getRoot();
}
