package de.core.utils;

import java.nio.file.Path;

public class FileUtils {
  public static String getFileExtension(Path p) {
    String filename = p.toString();
    if (filename.contains("."))
      return filename.substring(filename.lastIndexOf(".") + 1); 
    return null;
  }
}
