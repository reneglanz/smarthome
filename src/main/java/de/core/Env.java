package de.core;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.ClassOverride;
import de.core.serialize.annotation.Injectable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env implements Serializable {
  private static List<Class<?>> overrides = new ArrayList<>();
  
  private static Map<Object, Object> objects = Collections.synchronizedMap(new HashMap<>());
  
  private static boolean overridesLoaded = false;
  
  public static void put(Object key, Object value) throws CoreException {
    if (objects.containsKey(key))
      CoreException.throwCoreException("Object with key " + key.toString() + " already exists"); 
    objects.put(key, value);
  }
  
  public static void put(Object value) throws CoreException {
    if (value instanceof Injectable)
      putInjectable(value); 
    put(value.getClass(), value);
  }
  
  public static void remove(Object value) throws CoreException {
    objects.remove(value);
  }
  
  private static void putInjectable(Object injectable) throws CoreException {
    Injectable injectAnno = injectable.getClass().<Injectable>getAnnotation(Injectable.class);
    if (injectAnno != null && injectAnno.key().length() > 0) {
      put(injectAnno.key(), injectable);
    } else {
      put(injectable.getClass(), injectable);
    } 
  }
  
  public static <T> T get(Object key) {
    if (key instanceof Class) {
      Injectable injectAnno = key.getClass().<Injectable>getAnnotation(Injectable.class);
      if (injectAnno != null) {
        T injecteable = get(injectAnno.key());
        if (injecteable != null)
          return injecteable; 
      } 
    } 
    return (T)objects.get(key);
  }
  
  public static synchronized void loadOverrides() {
    ClassLoader classloader = ClassLoader.getSystemClassLoader();
    if (classloader instanceof URLClassLoader) {
      URLClassLoader urlClassLoader = (URLClassLoader)classloader;
      try {
        Enumeration<URL> enumeration = urlClassLoader.findResources("META-INF/overrides");
        while (enumeration.hasMoreElements()) {
          URL url = enumeration.nextElement();
          BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
          String line = "";
          while ((line = reader.readLine()) != null) {
            Class<?> override = Class.forName(line);
            ClassOverride anno = override.<ClassOverride>getAnnotation(ClassOverride.class);
            if (anno != null)
              overrides.add(override); 
          } 
        } 
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
  }
  
  public static Class<?> getOverrideFor(Class<?> clazz) throws CoreException {
    if (!overridesLoaded) {
      overridesLoaded = true;
      loadOverrides();
    } 
    for (Class<?> override : overrides) {
      if (override.getSuperclass().equals(clazz))
        return override; 
    } 
    return null;
  }
  
  public static void override(Class<?> clazz) {
    overrides.add(clazz);
  }
}
