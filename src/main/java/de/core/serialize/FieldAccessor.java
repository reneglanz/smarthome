package de.core.serialize;

import de.core.CoreException;
import de.core.Env;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldAccessor {
  private Field f;
  private Element anno;
  private Injectable inject;
  
  public static class FieldAccessorFactory {
    public static List<FieldAccessor> create(Class<?> clazz) {
      List<Field> fields = ReflectionHelper.getAllDeclaredFields(clazz);
      List<FieldAccessor> accessors = new ArrayList<>();
      for (Field f : fields) {
        if (f.getAnnotation(Element.class) != null || f
          .getAnnotation(Injectable.class) != null)
          accessors.add(new FieldAccessor(f)); 
      } 
      return accessors;
    }
  }
  
  public FieldAccessor(Field f) {
    this.f = f;
    if (f != null) {
      this.f.setAccessible(true);
      this.anno = f.<Element>getAnnotation(Element.class);
      this.inject = f.<Injectable>getAnnotation(Injectable.class);
    } 
  }
  
  public String name() {
    if ("".equals(this.anno.name()))
      return this.f.getName(); 
    return this.anno.name();
  }
  
  public Object get(Object o) throws CoreException {
    try {
      return this.f.get(o);
    } catch (Throwable t) {
      throw CoreException.throwCoreException(t);
    } 
  }
  
  public void set(Object obj, Object value) throws CoreException {
    try {
      if (value == null && this.anno.defaultValue().length() > 0) {
        setDefaultValue(obj);
      } else {
        this.f.set(obj, value);
      } 
    } catch (Throwable t) {
      throw CoreException.throwCoreException(t);
    } 
  }
  
  public boolean inject(Object obj) throws CoreException {
    try {
      if (this.inject != null) {
        Object key;
        Object injectable = null;
        if (this.inject.key().length() > 0) {
          key = this.inject.key();
        } else {
          key = this.f.getType();
        } 
        if (ServiceProvider.class.isAssignableFrom(this.f.getType()) && key instanceof Class) {
          injectable = Services.getProvider((Class)key);
        } else if (ServiceProvider.class.isAssignableFrom(this.f.getType()) && key instanceof String) {
          injectable = Services.getProvider((String)key);
        } else {
          injectable = Env.get(key);
        } 
        if (injectable == null && this.inject.selfInjecting()) {
          ClassAccessor classAccessor = ClassAccessor.create(this.f.getType());
          injectable = classAccessor.newInstance();
          if (injectable != null)
            if (this.inject.key().length() > 0) {
              Env.put(this.inject.key(), injectable);
            } else {
              Env.put(injectable);
            }  
        } 
        if (injectable != null) {
          this.f.set(obj, injectable);
          return true;
        } 
        return false;
      } 
      return true;
    } catch (Throwable t) {
      throw CoreException.throwCoreException(t);
    } 
  }
  
  @SuppressWarnings("deprecation")
public void setDefaultValue(Object obj) throws CoreException {
    if (this.anno != null && !"".equals(this.anno.defaultValue())) {
      Class<?> type = this.f.getType();
      Object value = null;
      if (type.equals(int.class)) {
        value = new Integer(this.anno.defaultValue());
      } else if (type.equals(long.class)) {
        value = new Long(this.anno.defaultValue());
      } else if (type.equals(byte.class)) {
        value = new Byte(this.anno.defaultValue());
      } else if (type.equals(float.class)) {
        value = new Float(this.anno.defaultValue());
      } else if (type.equals(double.class)) {
        value = new Double(this.anno.defaultValue());
      } else if (type.equals(char.class)) {
        value = new Character(this.anno.defaultValue().charAt(0));
      } else if (type.equals(boolean.class)) {
        value = new Boolean(this.anno.defaultValue());
      } else if (type.equals(String.class)) {
        value = this.anno.defaultValue();
      } else if (type.isEnum()) {
        value = Enum.valueOf((Class)this.f.getType(), this.anno.defaultValue());
      } 
      if (value != null)
        set(obj, value); 
    } 
  }
  
  public boolean mandatory() {
    return (this.anno != null && this.anno.mandatory());
  }
  
  public Class<?> getGenericType() {
    return this.anno.genericValueType();
  }
  
  public Class getType(Object obj) throws CoreException {
    if (this.f.getType().equals(Object.class)||this.getType().isInterface()) {
      Object value = get(obj);
      if (value != null)
        return value.getClass(); 
      return this.f.getType();
    } 
    return this.f.getType();
  }
  
  public Class getType() throws CoreException {
    return this.f.getType();
  }
  
  public boolean isFieldTypeObject() {
    return this.f.getType().equals(Object.class);
  }
  
  public String toString() {
    return "[Field:" + this.f.getName() + "]";
  }
  
  public boolean serialize() {
    return (this.anno != null && this.anno.serialize());
  }
  
  public boolean deserialize() {
    return (this.anno != null && this.anno.deserialize());
  }
  
  public boolean inline() {
    return (this.anno != null && this.anno.inline());
  }
  
  public Class[] inlineCasses() {
	  return this.anno.inlineClasses();
  }
}
