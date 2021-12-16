package de.core.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryStreamFilter implements DirectoryStream.Filter<Path> {
  private String regex;
  
  private boolean onlyFiles = true;
  
  public DirectoryStreamFilter(String pattern) {
    this(pattern, true);
  }
  
  public DirectoryStreamFilter(String pattern, boolean onlyFiles) {
    this.onlyFiles = onlyFiles;
    if (pattern.contains("*"))
      this.regex = pattern.replace("*", ".*"); 
  }
  
  public boolean accept(Path entry) throws IOException {
    boolean accept = false;
    if (this.onlyFiles && !Files.isDirectory(entry, new java.nio.file.LinkOption[0])) {
      accept = true;
    } else if (!this.onlyFiles) {
      accept = true;
    } 
    return (accept && entry.toString().matches(this.regex));
  }
}
