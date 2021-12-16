package de.core.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.List;

import de.core.serialize.Serializable;

public interface CallResponse extends Serializable {
  Object getValue();
  
  static CallResponse create(Object obj) {
    if (List.class.isAssignableFrom(obj.getClass()))
      return new ListCallResponse((List<?>)obj); 
    if (obj instanceof Throwable) {
      Throwable t = (Throwable)obj;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      t.printStackTrace(new PrintStream(baos));
      String source = null;
      try {
        source = InetAddress.getLocalHost().getHostName();
      } catch (Throwable throwable) {}
      try {
        return new ExceptionResponse(t.getLocalizedMessage(), source, new String(baos.toByteArray(), "UTF-8"));
      } catch (Throwable t3) {
        return new ExceptionResponse(t.getLocalizedMessage(), source, new String(baos.toByteArray()));
      } 
    } 
    if(obj.getClass().isArray()) {
    	if(obj.getClass().equals(byte[].class)) {
    		return new ByteArrayCallResponse((byte[]) obj);
    	} else if(obj.getClass().equals(int[].class)) {
    		return new IntArrayCallResponse((int[])obj);
    	} else if(obj.getClass().equals(double[].class)) {
     		return new DoubleArrayCallResponse((double[])obj);
     	} else if(obj.getClass().equals(long[].class)) {
     		return new LongArrayCallResponse((long[])obj);
     	} else if(obj.getClass().equals(char[].class)) {
     		return new CharArrayCallResponse((char[])obj);
     	}
    }
    return new ObjectCallResponse(obj);
  }
}
