package de.core.serialize.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class SJOSParser implements Parser {
  public void parse(Reader is, CodingReadHandler handler) throws IOException {
    int read = 0;
    int pos = 0;
    StringBuffer buffer = new StringBuffer();
    String name = "";
    DefaultReadHandler handler0 = (DefaultReadHandler)handler;
    boolean comment = false;
    while (!handler.isComplete()) {
      read = is.read();
      pos++;
      if (read == -1)
        return; 
      char c = (char)read;
      if ('\r' == c || '\n' == c) {
        if (comment) {
          buffer.setLength(0);
          comment = false;
          continue;
        } 
        String trimmed = buffer.toString().trim();
        if (trimmed.length() > 0)
          handler.value(null, trimmed); 
        buffer = new StringBuffer();
        continue;
      } 
      if (comment)
        continue; 
      if ('#' == c) {
        comment = true;
        continue;
      } 
      if ('{' == c) {
        handler.startObject(name);
        name = null;
        buffer = new StringBuffer();
        continue;
      } 
      if ('}' == c) {
        handler.endObject(name);
        name = null;
        buffer = new StringBuffer();
        continue;
      } 
      if ('[' == c) {
        handler.startArray(name);
        name = null;
        buffer = new StringBuffer();
        continue;
      } 
      if (']' == c) {
        handler.endArray(name);
        name = null;
        buffer = new StringBuffer();
        continue;
      } 
      if ('=' == c) {
        name = buffer.toString().trim();
        buffer = new StringBuffer();
        String value = parseValue(is);
        if (value != null) {
          if ("@type".equals(name)) {
            handler.setType(value);
            continue;
          } 
          handler.value(name, value);
        } 
        continue;
      } 
      buffer.append(c);
    } 
  }
  
  private String parseValue(Reader is) throws IOException {
    StringBuffer buffer = new StringBuffer();
    boolean escapeNext = false;
    int read = 0;
    is.mark(0);
    while ((read = is.read()) != -1) {
      char c = (char)read;
      if (!escapeNext && ('{' == c || '}' == c || '[' == c || ']' == c || '=' == c)) {
        is.reset();
        return null;
      } 
      if (!escapeNext && ('\n' == c || '\r' == c))
        return buffer.toString().trim(); 
      if ('\\' == c && !escapeNext) {
        escapeNext = true;
        continue;
      } 
      escapeNext = false;
      buffer.append(c);
    } 
    return "";
  }
}
