package de.core.serialize.elements;

import de.core.CoreException;
import de.core.Env;

public class PrimitivElement extends Element {
  protected Object value;
  
  protected Object resolvedValue;
  
  public PrimitivElement(Element parent) {
    super(parent);
  }
  
  public PrimitivElement(Element parent, String name, Object value) {
    super(parent);
    this.name = name;
    this.value = value;
  }
  
  public String asString() {
    return (this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString();
  }
  
  public Byte asByte() {
    return Byte.valueOf(Byte.parseByte((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public Integer asInteger() {
    return Integer.valueOf(Integer.parseInt((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public Long asLong() {
    return Long.valueOf(Long.parseLong((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public Float asFloat() {
    return Float.valueOf(Float.parseFloat((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public Double asDouble() {
    return Double.valueOf(Double.parseDouble((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public Character asCharacter() {
    return Character.valueOf((this.resolvedValue != null) ? this.resolvedValue.toString().charAt(0) : this.value.toString().charAt(0));
  }
  
  public Boolean asBoolean() {
    return Boolean.valueOf(Boolean.parseBoolean((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public Short asShort() {
    return Short.valueOf(Short.parseShort((this.resolvedValue != null) ? this.resolvedValue.toString() : this.value.toString()));
  }
  
  public boolean isNull() {
    return (this.value == null);
  }
  
  public <T> T as(Class<T> type) throws CoreException {
    if (type != null) {
      resolve();
      if (type.equals(Integer.class) || type.equals(int.class))
        return (T)asInteger(); 
      if (type.equals(Long.class) || type.equals(long.class))
        return (T)asLong(); 
      if (type.equals(Byte.class) || type.equals(byte.class))
        return (T)asByte(); 
      if (type.equals(Float.class) || type.equals(float.class))
        return (T)asFloat(); 
      if (type.equals(Double.class) || type.equals(double.class))
        return (T)asDouble(); 
      if (type.equals(Character.class) || type.equals(char.class))
        return (T)asCharacter(); 
      if (type.equals(Boolean.class) || type.equals(boolean.class))
        return (T)asBoolean(); 
      if (type.equals(Short.class) || type.equals(short.class))
        return (T)asShort(); 
      if (type.equals(String.class))
        return (T)asString(); 
      if (type.isEnum())
        return (T)Enum.valueOf((Class)type, asString()); 
      if (type.equals(Class.class))
        try {
          return (T)Class.forName(asString());
        } catch (ClassNotFoundException e) {
          throw CoreException.throwCoreException(e);
        }  
    } 
    return null;
  }
  
  public Element asSJOSElement() {
    return (Element)this.value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public String toString() {
    return "SJOSPrimitivElement[name=" + this.name + "; value=" + getValue() + "]";
  }
  
  public Root getRoot() {
    return getParent().getRoot();
  }
  
  public void resolve() throws CoreException {
    if (this.resolvedValue == null) {
      String tmp = this.value.toString();
      StringBuilder builder = new StringBuilder();
      StringBuilder entityName = new StringBuilder();
      boolean entity = false;
      for (int i = 0; i < tmp.length(); i++) {
        if (tmp.charAt(i) == '&') {
          entity = true;
        } else if (tmp.charAt(i) == ';' && entity) {
          if (entityName.length() > 2) {
            String envValue = (String)Env.get(entityName.toString().substring(0, entityName.length()));
            if (envValue != null) {
              builder.append(envValue);
            } else {
              CoreException.throwCoreException("Entity [name=" + entityName + "] not defined in this environment");
            } 
          } else {
            CoreException.throwCoreException("Entity not complete for " + this.name + " value[=" + this.value + "]");
          } 
          entity = false;
        } else if (entity) {
          entityName.append(tmp.charAt(i));
        } else {
          builder.append(tmp.charAt(i));
        } 
      } 
      this.resolvedValue = builder.toString()+(entity&&entityName.length()>0?entityName.toString():"");
    } 
  }
  
  public Object getValue() {
    return this.value;
  }
}
