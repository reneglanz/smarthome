package de.core.rt;

import de.core.CoreException;
import de.core.Env;
import de.core.serialize.Coding;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launch {
  public Launch(String[] args) throws IOException, CoreException {
    InputStream is = null;
    if (args.length == 1) {
      is = new FileInputStream(new File(args[0]));
    } else {
      is = System.in;
    } 
    if (System.getProperty("install.dir") != null) {
      Env.put("install.dir", System.getProperty("install.dir"));
    } else {
      Path userDir = Paths.get(System.getProperty("user.dir"), new String[0]);
      Env.put("install.dir", userDir.getParent().toString());
    } 
    Object o = Coding.decode(is, "sjos");
    if (o instanceof Launchable) {
      Launchable launchable = (Launchable)o;
      launchable.launch();
    } else {
      throw new RuntimeException("Object must implements Launchable");
    } 
  }
  
  public static void main(String[] args) {
    try {
      new Launch(args);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CoreException e) {
      e.printStackTrace();
    } 
  }
}
