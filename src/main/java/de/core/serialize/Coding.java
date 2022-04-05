package de.core.serialize;

import de.core.CoreException;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Root;
import de.core.serialize.parser.CodingReadHandler;
import de.core.serialize.parser.DefaultReadHandler;
import de.core.serialize.parser.JsonParser;
import de.core.serialize.parser.Parser;
import de.core.serialize.parser.SJOSParser;
import de.core.serialize.writer.CodingWriter;
import de.core.serialize.writer.DefaultWriter;
import de.core.serialize.writer.JsonWriter;
import de.core.serialize.writer.SJOSWriter;
import de.core.utils.FileUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class Coding {
  private static SJOSDeserializer deserializer = new SJOSDeserializer();
  
  private static SJOSSerializer serializer = new SJOSSerializer();
  
  private static Map<String, Parser> parser = new HashMap<>();
  
  static {
    parser.put("sjos", new SJOSParser());
    parser.put("json", new JsonParser());
  }
  
  private static Map<String, CodingWriter> codingWriter = new HashMap<>();
  
  static {
    codingWriter.put("sjos", new SJOSWriter());
    codingWriter.put("json", new JsonWriter());
  }
  
  public static <T> T decode(InputStream is) throws CoreException {
    return decode(is, "sjos", (Class<?>)null);
  }
  
  public static <T> T decode(String data) throws CoreException {
    return decode(data.getBytes(), "sjos");
  }
  
  public static <T> T decode(String data, String extension) throws CoreException {
    return decode(data.getBytes(), extension);
  }
  
  public static <T> T decode(String data, String extension, Class<?> clazz) throws CoreException {
    return decode(data.getBytes(), extension, clazz);
  }
  
  public static <T> T decode(byte[] data, String extension, Class<?> clazz) throws CoreException {
    return decode(new ByteArrayInputStream(data), extension, clazz);
  }
  
  public static <T> T decode(byte[] data, String extension) throws CoreException {
    return decode(new ByteArrayInputStream(data), extension, (Class<?>)null);
  }
  
  public static <T> T decode(byte[] data) throws CoreException {
    return decode(new ByteArrayInputStream(data), "sjos", (Class<?>)null);
  }
  
  public static <T> T decode(Path path) throws CoreException {
    try {
      return decode(Files.readAllBytes(path), FileUtils.getFileExtension(path));
    } catch (Throwable t) {
      if (t instanceof CoreException)
        throw (CoreException)t; 
      throw CoreException.throwCoreException(t);
    } 
  }
  
  public static <T> T decode(Class<T> clazz) throws CoreException {
    InputStream is = clazz.getResourceAsStream(clazz.getSimpleName() + ".sjos");
    return decode(is);
  }
  
  public static <T> T decode(InputStream is, String extension) throws CoreException {
    return decode(is.markSupported() ? is : new BufferedInputStream(is, 16384), extension, (Class<?>)null);
  }
  
  public static <T> T decode(InputStream is, String extension, Class<?> clazz) throws CoreException {
    Parser _parser = parser.get(extension);
    if (_parser != null) {
      DefaultReadHandler handler = new DefaultReadHandler();
      try {
        _parser.parse(new BufferedReader(new InputStreamReader(is, "UTF-8")), (CodingReadHandler)handler);
        Root root = handler.getResult();
        return deserializer.deserialze((ComplexElement)root, clazz);
      } catch (Exception e) {
        throw CoreException.throwCoreException(e);
      } 
    } 
    return (T)CoreException.throwCoreException("No parser for " + extension + " found");
  }
  
  public static void encode(Object object, OutputStream os, String extension) throws CoreException {
    encode(object, os, extension, false);
  }
  
  public static void encode(Object object, OutputStream os, String extension, boolean skipTypes) throws CoreException {
    CodingWriter _codingWriter = codingWriter.get(extension);
    if (_codingWriter != null) {
      DefaultWriter writer = new DefaultWriter();
      writer.skipType = skipTypes;
      writer.write(serializer.serialize(object), os, _codingWriter);
    } 
  }
   
  public static byte[] encode(Object object, String extension) throws CoreException {
    return encode(object, extension, false);
  }
  
  public static byte[] encode(Object object, String extension, boolean skipTypes) throws CoreException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    encode(object, baos, extension, skipTypes);
    return baos.toByteArray();
  }
  
  public static byte[] encode(Object object) throws CoreException {
    return encode(object, "sjos");
  }
  
  public static void encode(Object object, OutputStream os) throws CoreException {
    encode(object, os, "sjos");
  }
  
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0xF];
    } 
    return new String(hexChars);
  }
  
  public static String toBase64(byte[] bytes) {
    Base64.Encoder base64 = Base64.getEncoder();
    return base64.encodeToString(bytes);
  }
  
  public static byte[] compress(byte[] ba) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      GZIPOutputStream gzip = new GZIPOutputStream(out);
      gzip.write(ba);
      gzip.close();
      return out.toByteArray();
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    } 
  }
  
  public static byte[] fromBase64(String encoded) {
    Base64.Decoder base64 = Base64.getDecoder();
    return base64.decode(encoded);
  }
  
}
