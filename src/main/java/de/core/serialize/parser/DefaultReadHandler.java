package de.core.serialize.parser;

import de.core.serialize.elements.Array;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Element;
import de.core.serialize.elements.PrimitivElement;
import de.core.serialize.elements.Root;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DefaultReadHandler implements CodingReadHandler {
  Root root;
  
  public String parseSimpleValue(InputStream is) throws IOException {
    return parseEntityValue(is, false);
  }
  
  private String parseEntityValue(InputStream is, boolean parseEntity) throws IOException {
    StringBuffer buffer = new StringBuffer();
    boolean complex = false, first = true, escapeNext = false;
    int read = 0;
    while ((read = is.read()) != -1) {
      char c = (char)read;
      if ('{' == c && first && parseEntity) {
        buffer.append(c);
        complex = true;
        continue;
      } 
      if (!escapeNext && !complex && ('\n' == c || '\r' == c))
        return buffer.toString().trim(); 
      if ('\\' == c) {
        escapeNext = true;
        continue;
      } 
      if (complex && parseEntity && '}' == c) {
        buffer.append(c);
        return buffer.toString().trim();
      } 
      escapeNext = false;
      first = false;
      buffer.append(c);
    } 
    return "";
  }
  
  List<ComplexElement> complexList = new ArrayList<>();
  
  private boolean complete = false;
  
  public ComplexElement getLastComplex() {
    if (this.complexList.size() > 0)
      return this.complexList.get(this.complexList.size() - 1); 
    return (ComplexElement)this.root;
  }
  
  private void removeLast() {
    if (this.complexList.size() > 0)
      this.complexList.remove(this.complexList.size() - 1); 
  }
  
  private void addComplex(ComplexElement newComlex) {
    this.complexList.add(newComlex);
  }
  
  public void startObject(String name) {
    ComplexElement parent = getLastComplex();
    ComplexElement complex = null;
    if (parent == null) {
      this.root = new Root();
      Root root = this.root;
      complex=root;
    } else {
      complex = new ComplexElement((Element)parent);
      parent.add((Element)complex);
      complex.setName(name);
    } 
    addComplex(complex);
  }
  
  public void endObject(String name) {
    removeLast();
    if (this.complexList.size() == 0)
      this.complete = true; 
  }
  
  public void startArray(String name) {
    ComplexElement parent = getLastComplex();
    ComplexElement complex = null;
    Array array = new Array((Element)parent);
    parent.add((Element)array);
    array.setName(name);
    addComplex((ComplexElement)array);
  }
  
  public void endArray(String name) {
    removeLast();
  }
  
  public void value(String objectName, String value) {
    ComplexElement parent = getLastComplex();
    PrimitivElement primitiv = new PrimitivElement((Element)parent);
    primitiv.setName(objectName);
    primitiv.setValue(value);
    parent.add((Element)primitiv);
  }
  
  public void setType(String type) {
    ComplexElement last = getLastComplex();
    if (last != null)
      last.setType(type); 
  }
  
  public Root getResult() {
    return this.root;
  }
  
  public boolean isComplete() {
    return this.complete;
  }
}
