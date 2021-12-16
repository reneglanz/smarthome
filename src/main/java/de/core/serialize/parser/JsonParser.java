package de.core.serialize.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class JsonParser implements Parser {
  public void parse(Reader is, CodingReadHandler handler) throws IOException {
    int read = 0;
    int pos = 0;
    StringBuffer buffer = new StringBuffer();
    boolean valueNext = false;
    String name = "";
    boolean inquotes = false;
    DefaultReadHandler handler0 = (DefaultReadHandler)handler;
    while (!handler.isComplete()) {
      read = is.read();
      pos++;
      if (read == -1)
        return; 
      char c = (char)read;
      if (('\r' == c || '\n' == c || ',' == c || '}' == c || ']' == c) && valueNext && buffer.toString().trim().length() > 0) {
        String value = buffer.toString().trim();
        handler.value(name, value);
        buffer = new StringBuffer();
        valueNext = false;
        if (c == ']') {
          handler.endArray("");
          continue;
        } 
        if (c == '}')
          handler.endObject(""); 
        continue;
      } 
      if ('{' == c) {
        handler.startObject(name);
        name = null;
        valueNext = false;
        buffer = new StringBuffer();
        continue;
      } 
      if ('}' == c) {
        handler.endObject(name);
        name = null;
        valueNext = false;
        buffer = new StringBuffer();
        continue;
      } 
      if ('[' == c) {
        handler.startArray(name);
        name = null;
        valueNext = true;
        buffer = new StringBuffer();
        continue;
      } 
      if (']' == c) {
        handler.endArray(name);
        name = null;
        valueNext = false;
        buffer = new StringBuffer();
        continue;
      } 
      if ('"' == c) {
        inquotes = !inquotes;
        if (valueNext && !inquotes) {
          buffer.append(c);
          String value = removeQuotes(buffer.toString().trim());
          if ("@type".equals(name)) {
            handler.setType(value);
          } else {
            handler.value(name, value);
          } 
          name = null;
          valueNext = handler0.getLastComplex() instanceof de.core.serialize.elements.Array;
          buffer = new StringBuffer();
          continue;
        } 
        buffer.append(c);
        continue;
      } 
      if (':' == c && !inquotes) {
        name = removeQuotes(buffer.toString().trim());
        buffer = new StringBuffer();
        valueNext = true;
        continue;
      } 
      if (',' == c)
        continue; 
      buffer.append(c);
    } 
  }
  
  private String removeQuotes(String value) throws IOException {
    if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1)
      return value.substring(1, value.length() - 1); 
    throw new IOException("Missing quotes next to " + value);
  }
  
  private String parseValue(InputStream is, boolean parseEntity) throws IOException {
    StringBuffer buffer = new StringBuffer();
    int read = 0;
    while ((read = is.read()) != -1) {
      char c = (char)read;
      buffer.append(c);
      if (c == '"')
        return buffer.toString().trim(); 
    } 
    return "";
  }
}
