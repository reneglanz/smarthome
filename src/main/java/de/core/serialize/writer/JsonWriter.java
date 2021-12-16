package de.core.serialize.writer;

import de.core.CoreException;
import java.io.OutputStream;

public class JsonWriter implements CodingWriter {
  private static final String charset = "UTF-8";
  
  private static final String EOL = "\n";
  
  private static final String START_OBJ = "{";
  
  private static final String END_OBJ = "}";
  
  private static final String START_ARR = "[";
  
  private static final String END_ARR = "]";
  
  private static final String TYPE = "@type";
  
  private static final String VALUE_ASSIGN = ":";
  
  private static final String ESCAPE = "\\";
  
  private static final String QUOTES = "\"";
  
  private static final String COMMA = ",";
  
  private static final String[] TO_ESCAPE = new String[] { "\\", "\r\n", "\n" };
  
  private static final byte[] TAB = new byte[] { 9 };
  
  private int inset = 0;
  
  public void writeStartObject(OutputStream os, String name) throws CoreException {
    if (name != null)
      write(os, this.inset, new String[] { "\"", name, "\"", ":" }); 
    write(os, (name != null) ? 0 : this.inset, new String[] { "{", "\n" });
    this.inset++;
  }
  
  public void writeEndObject(OutputStream os, String name) throws CoreException {
    this.inset--;
    write(os, 0, new String[] { "\n" });
    write(os, this.inset, new String[] { "}" });
  }
  
  public void writeStartArray(OutputStream os, String name) throws CoreException {
    if (name != null)
      write(os, this.inset, new String[] { "\"", name, "\"", ":" }); 
    write(os, (name != null) ? 0 : this.inset, new String[] { "[", "\n" });
    this.inset++;
  }
  
  public void writeEndArray(OutputStream os, String name) throws CoreException {
    this.inset--;
    write(os, 0, new String[] { "\n" });
    write(os, this.inset, new String[] { "]" });
  }
  
  public void writeValue(OutputStream os, String name, Object value) throws CoreException {
    if (name != null)
      write(os, this.inset, new String[] { "\"", name, "\"", ":" }); 
    if (value != null) {
      if (needQuotes(value)) {
        write(os, (name != null) ? 0 : this.inset, new String[] { "\"", escape(value.toString()), "\"" });
      } else {
        write(os, (name != null) ? 0 : this.inset, new String[] { escape(value.toString()) });
      } 
    } else {
      write(os, (name != null) ? 0 : this.inset, new String[] { "null" });
    } 
  }
  
  private boolean needQuotes(Object value) {
    Class<?> clazz = value.getClass();
    if (int.class.equals(clazz) || long.class
      .equals(clazz) || byte.class
      .equals(clazz) || boolean.class
      .equals(clazz) || double.class
      .equals(clazz) || float.class
      .equals(clazz) || short.class
      .equals(clazz) || Integer.class
      .equals(clazz) || Long.class
      .equals(clazz) || Byte.class
      .equals(clazz) || Boolean.class
      .equals(clazz) || Double.class
      .equals(clazz) || Float.class
      .equals(clazz))
      return false; 
    return true;
  }
  
  public void writeType(OutputStream os, String type) throws CoreException {
    write(os, this.inset, new String[] { "\"", "@type", "\"", ":", "\"", type, "\"" });
  }
  
  private String escape(String value) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < value.length(); i++) {
      for (int j = 0; j < TO_ESCAPE.length; j++) {
        if (TO_ESCAPE[j].equals("" + value.charAt(i))) {
          buffer.append("\\");
          break;
        } 
      } 
      buffer.append(value.charAt(i));
    } 
    return buffer.toString();
  }
  
  private void write(OutputStream os, int inset, String... value) throws CoreException {
    try {
      for (int i = 0; i < inset; i++)
        os.write(TAB); 
      for (String v : value)
        os.write(v.getBytes("UTF-8")); 
    } catch (Throwable t) {
      throw CoreException.throwCoreException(t);
    } 
  }
  
  public void writeElementSeparator(OutputStream os) throws CoreException {
    write(os, 0, new String[] { ",", "\n" });
  }
}
