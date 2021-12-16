package de.core.serialize.writer;

import de.core.CoreException;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Element;
import de.core.serialize.elements.PrimitivElement;
import java.io.OutputStream;

public class DefaultWriter implements Writer {
  public boolean skipType = false;
  
  public void write(ComplexElement root, OutputStream os, CodingWriter handler) throws CoreException {
    if (root instanceof de.core.serialize.elements.Array) {
      handler.writeStartArray(os, root.getName());
    } else {
      handler.writeStartObject(os, root.getName());
    } 
    if (!this.skipType && root.getType() != null) {
      handler.writeType(os, root.getType());
      if (root.getChildren().size() > 0)
        handler.writeElementSeparator(os); 
    } 
    for (int i = 0; i < root.getChildren().size(); i++) {
      Element child = root.getChildren().get(i);
      try {
        if (child instanceof PrimitivElement) {
          handler.writeValue(os, child.getName(), ((PrimitivElement)child).getValue());
        } else {
          write((ComplexElement)child, os, handler);
        } 
        if (root.getChildren().size() > i + 1)
          handler.writeElementSeparator(os); 
      } catch (Exception exception) {}
    } 
    if (root instanceof de.core.serialize.elements.Array) {
      handler.writeEndArray(os, root.getName());
    } else {
      handler.writeEndObject(os, root.getName());
    } 
  }
}
