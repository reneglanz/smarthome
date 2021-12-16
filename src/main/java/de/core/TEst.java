package de.core;

import de.core.serialize.Coding;
import de.core.service.CallResponse;
import java.io.IOException;

public class TEst {
  public static void main(String[] args) throws CoreException {
    byte[] ba = Coding.encode(CallResponse.create(new IOException("test[x1]")));
    System.out.println(new String(ba));
    Object o = Coding.decode(ba);
  }
}
