package de.core.serialize.parser;

import de.core.serialize.elements.Root;

public interface CodingReadHandler {
  void startObject(String paramString);
  
  void endObject(String paramString);
  
  void startArray(String paramString);
  
  void endArray(String paramString);
  
  void value(String paramString1, String paramString2);
  
  void setType(String paramString);
  
  Root getResult();
  
  boolean isComplete();
}
