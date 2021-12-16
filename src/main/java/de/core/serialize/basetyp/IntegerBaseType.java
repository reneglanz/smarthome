package de.core.serialize.basetyp;

public class IntegerBaseType implements BaseType<Integer> {
  public Class<?> getBaseClass() {
    return Integer.class;
  }
  
  public Integer parse(String value) {
    return Integer.valueOf(Integer.parseInt(value));
  }
}
