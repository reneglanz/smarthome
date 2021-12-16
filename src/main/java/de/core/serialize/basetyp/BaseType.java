package de.core.serialize.basetyp;

public interface BaseType<T> {
  Class<?> getBaseClass();
  
  T parse(String paramString);
}
