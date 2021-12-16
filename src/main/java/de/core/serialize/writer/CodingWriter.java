package de.core.serialize.writer;

import de.core.CoreException;
import java.io.OutputStream;

public interface CodingWriter {
  void writeStartObject(OutputStream paramOutputStream, String paramString) throws CoreException;
  
  void writeEndObject(OutputStream paramOutputStream, String paramString) throws CoreException;
  
  void writeStartArray(OutputStream paramOutputStream, String paramString) throws CoreException;
  
  void writeEndArray(OutputStream paramOutputStream, String paramString) throws CoreException;
  
  void writeValue(OutputStream paramOutputStream, String paramString, Object paramObject) throws CoreException;
  
  void writeType(OutputStream paramOutputStream, String paramString) throws CoreException;
  
  void writeElementSeparator(OutputStream paramOutputStream) throws CoreException;
}
