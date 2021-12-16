package de.core.serialize;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionHelper {
  public static List<Field> getAllDeclaredFields(Class<?> clazz) {
    return getAllDeclaredFields(new ArrayList<>(), clazz);
  }
  
  private static List<Field> getAllDeclaredFields(List<Field> fields, Class<?> clazz) {
    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
    if (clazz.getSuperclass() != null)
      getAllDeclaredFields(fields, clazz.getSuperclass()); 
    return fields;
  }
}
