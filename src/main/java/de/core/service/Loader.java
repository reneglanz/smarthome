package de.core.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.core.log.Logger;
import de.core.rt.Resource;
import de.core.serialize.Coding;
import de.core.utils.DirectoryStreamFilter;

public class Loader<E> {
  Logger logger = Logger.createLogger("Loader");
  
  private Path dir;
  
  private DirectoryStreamFilter filter;
  
  public Loader(Path dir, DirectoryStreamFilter filter) {
    this.dir = dir;
    this.filter = filter;
  }
  
  public Loader(Path dir) {
    this(dir, new DirectoryStreamFilter("*"));
  }
  
  public void load(BiConsumer<Path, Object> action) throws IOException {
    DirectoryStream<Path> dirstream = Files.newDirectoryStream(this.dir, (DirectoryStream.Filter<? super Path>)this.filter);
    dirstream.forEach(p -> {
          try {
            Object loaded = Coding.decode(Files.readAllBytes(p), "sjos");
            action.accept(p,loaded);
          } catch (Throwable t) {
            this.logger.error("Failed to load file " + p.toString(), t);
          } 
        });
  }
  
  public String normalizeName(Object object,String filename) {
	  if(filename.contains(".sjos")) {
		  if(object instanceof Service&&filename.endsWith(".service.sjos")) {
			  return filename.substring(0,filename.indexOf(".service.sjos"));
		  } else if(object instanceof Resource&&filename.endsWith(".resource.sjos")) {
			  return filename.substring(0,filename.indexOf(".resource.sjos"));
		  }
	  }
	  return filename;
  }
  
}
