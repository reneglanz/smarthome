package de.core.serialize;

import de.core.CoreException;
import de.core.Env;
import de.core.serialize.annotation.Injectable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class ClassAccessor {
  String className;
  
  Class<?> clazz;
  
  Injectable injectable;
  
  protected ClassAccessor(String className) {
    this.className = className;
  }
  
  protected ClassAccessor(Class<?> clazz) {
    this.clazz = clazz;
    this.className = clazz.getName();
  }
  
  public static ClassAccessor create(String className) {
    return new ClassAccessor(className);
  }
  
  public static ClassAccessor create(Class<?> clazz) {
    return new ClassAccessor(clazz);
  }
  
  public Class<?> get() throws CoreException {
    if (this.clazz == null)
      try {
        this.clazz = Class.forName(this.className);
        Class<?> override = Env.getOverrideFor(this.clazz);
        if (override != null)
          this.clazz = override; 
        this.injectable = this.clazz.<Injectable>getAnnotation(Injectable.class);
      } catch (Throwable t) {
        throw CoreException.throwCoreException(t);
      }  
    return this.clazz;
  }
  
  public Object newInstance() throws CoreException {
    try {
      if (this.clazz.getEnclosingClass() != null && Modifier.isStatic(this.clazz.getEnclosingClass().getModifiers())) {
        ClassAccessor enclosing = create(this.clazz.getEnclosingClass());
        Constructor<?> ctor = this.clazz.getDeclaredConstructor(new Class[] { enclosing.get() });
        Object enclosingObj = Env.get(enclosing.get());
        return ctor.newInstance(new Object[] { enclosingObj });
      } 
      try {
        Constructor<?> ctor = this.clazz.getDeclaredConstructor(new Class[0]);
        ctor.setAccessible(true);
        return ctor.newInstance(new Object[0]);
      } catch (NoSuchMethodException e) {
        CoreException.throwCoreException("Missing trivial constructor for " + this.clazz.toString());
      } 
    } catch (Throwable t) {
      CoreException.throwCoreException(t);
    } 
    return null;
  }
  
  public boolean isSerializable() throws CoreException {
    return Serializable.class.isAssignableFrom(get());
  }
  
  public boolean isInterface() throws CoreException {
	  return get().isInterface();
  }
  
  public boolean isBaseType() {
    return SJOSDeserializer.isBaseType(this.clazz);
  }
  
  public boolean isInjectable() {
    return (this.injectable != null);
  }
  
  public String getInjectKey() {
    return (this.injectable != null) ? ((this.injectable.key().length() > 0) ? this.injectable.key() : this.className) : null;
  }
}
