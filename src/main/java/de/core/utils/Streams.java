package de.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
  public static byte[] readAll(InputStream is) throws IOException {
    return read(is, -1);
  }
  
  public static byte[] read(InputStream is, int readMax) throws IOException {
    if(is==null) {
    	return new byte[0];
    }
	int read = 0;
    byte[] ba = new byte[(readMax != -1 && readMax < 16384) ? readMax : 65280];
    int readCnt = 0;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      while ((read = is.read(ba)) != -1) {
        os.write(ba, 0, read);
        readCnt += read;
        if (readMax != -1 && readCnt >= readMax)
          return os.toByteArray(); 
      } 
      return os.toByteArray();
    } 
  }
  
  public static void copy(InputStream is, OutputStream os) throws IOException {
    int read = 0;
    byte[] ba = new byte[16384];
    while ((read = is.read(ba)) != -1)
      os.write(ba, 0, read); 
  }
}
