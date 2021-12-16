package de.core.serialize.basetyp;

import java.util.ArrayList;
import java.util.List;

public class BaseTypes {
  List<BaseType<?>> baseTypes = new ArrayList<>();
  
  public BaseTypes() {
    this.baseTypes.add(new IntegerBaseType());
  }
  
  public BaseType<?> getBaseType(Class<?> clazz) {
    for (BaseType<?> baseType : this.baseTypes) {
      if (baseType.getBaseClass().equals(clazz))
        return baseType; 
    } 
    return null;
  }
}
