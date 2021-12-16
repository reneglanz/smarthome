package de.core.serialize.writer;

import de.core.CoreException;
import de.core.serialize.elements.ComplexElement;
import java.io.OutputStream;

public interface Writer {
  void write(ComplexElement paramComplexElement, OutputStream paramOutputStream, CodingWriter paramCodingWriter) throws CoreException;
}
