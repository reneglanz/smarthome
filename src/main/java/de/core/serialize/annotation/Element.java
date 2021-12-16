package de.core.serialize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Element {
  String name() default "";
  
  String defaultValue() default "";
  
  boolean mandatory() default false;
  Class<?> genericValueType() default String.class;
  boolean serialize() default true;
  boolean deserialize() default true;
  boolean inline() default false;
  Class<?>[] inlineClasses() default Class[].class;
}
