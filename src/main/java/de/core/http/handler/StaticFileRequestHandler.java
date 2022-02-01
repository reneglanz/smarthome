package de.core.http.handler;

import de.core.cache.Cache;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.mime.MimeTypes;
import de.core.log.Logger;
import de.core.serialize.annotation.Element;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.stream.Stream;

public class StaticFileRequestHandler extends AbstractHttpRequestHandler {
  @Element
  protected String root;
  
  @Element(defaultValue = "index.html")
  protected String welcomFile;
  
  @Element(defaultValue = "true")
  protected boolean cacheFiles = false;
  
  private Cache<String, byte[]> cache;
  
  private class ReloadThread extends Thread {
    Logger logger = Logger.createLogger("StaticFileRequestHandler.ReloadThread");
    
    WatchService watchService;
    
    private Cache<WatchKey, Path> watchkeys;
    
    protected ReloadThread() throws IOException {
      this.watchService = FileSystems.getDefault().newWatchService();
      this.watchkeys = new Cache();
    }
    
    public void run() {
      try {
        WatchKey key;
        while ((key = this.watchService.take()) != null) {
          for (WatchEvent<?> event : key.pollEvents()) {
            try {
              Path path = ((Path)this.watchkeys.get(key)).resolve((Path)event.context());
              if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                if (!Files.isDirectory(path, new java.nio.file.LinkOption[0])) {
                  this.logger.debug("Remove " + path.toString());
                  StaticFileRequestHandler.this.cache.invalidate(path.normalize().toString());
                } 
                continue;
              } 
              if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                if (Files.isDirectory(path, new java.nio.file.LinkOption[0])) {
                  this.logger.debug("Watch new directry " + path.toString());
                  watch(path);
                  continue;
                } 
                this.logger.debug("Load new file " + path.toString());
                StaticFileRequestHandler.this.cache.put(path.normalize().toString(), Files.readAllBytes(path));
                continue;
              } 
              if (!Files.isDirectory(path, new java.nio.file.LinkOption[0])) {
                this.logger.debug("Reload " + path.toString());
                StaticFileRequestHandler.this.cache.put(path.normalize().toString(), Files.readAllBytes(path));
              } 
            } catch (Throwable t) {
              t.printStackTrace();
            } 
          } 
          boolean valid = key.reset();
          if (!valid) {
            key.cancel();
            this.watchkeys.invalidate(key);
          } 
        } 
      } catch (Throwable t) {
        t.printStackTrace();
      } 
    }
    
    public void watch(Path dir) throws IOException {
      WatchKey key = dir.register(this.watchService, (WatchEvent.Kind<?>[])new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY });
      this.watchkeys.put(key, dir);
    }
  }
  
  Logger log = Logger.createLogger("StaticFileRequestHandler");
  
  public StaticFileRequestHandler(String contextPath, String root) {
    super(contextPath);
  }
  
  public void finish() {
    if (this.root.endsWith("/"))
      this.root = this.root.substring(0, this.root.length() - 1); 
    if (this.cacheFiles) {
      this.cache = new Cache();
      try {
        ReloadThread reload = new ReloadThread();
        reload.watch(Paths.get(this.root, new String[0]));
        try (Stream<Path> walker = Files.walk(Paths.get(this.root, new String[0]), new java.nio.file.FileVisitOption[0])) {
          walker.forEach(p -> {
                try {
                  if (!Files.isDirectory(p, new java.nio.file.LinkOption[0])) {
                    this.cache.put(p.normalize().toString(), Files.readAllBytes(p));
                  } else {
                    reload.watch(p);
                  } 
                } catch (IOException iOException) {}
              });
        } catch (IOException iOException) {}
        reload.start();
      } catch (Throwable throwable) {}
    } 
  }
  
  public HttpResponse handleRequest(HttpRequest request) {
    String requestedResource = request.getRequestPath();
    if (requestedResource.equals(this.contextPath.startsWith("/") ? this.contextPath : ("/" + this.contextPath))) {
      FixedLengthHttpResponse response = new FixedLengthHttpResponse("FORWARDED".getBytes());
      response.addHeader(new HttpHeader("Location", requestedResource + (this.welcomFile.startsWith("/") ? this.welcomFile : ("/" + this.welcomFile))));
      response.setStatusCode(302);
      return response;
    } 
    requestedResource = requestedResource.replace(this.contextPath, "");
    requestedResource = this.root + requestedResource;
    if (requestedResource.endsWith(this.contextPath))
      requestedResource = requestedResource + "/" + this.welcomFile; 
    Path p = FileSystems.getDefault().getPath(requestedResource, new String[0]).normalize();
    try {
      byte[] data;
      if(this.cacheFiles) {
    	  data=(byte[])this.cache.get(p.toString());
      } else {
    	  data=Files.readAllBytes(p);
      }
      HttpResponse response=null;
      if(data!=null) {
		  response = new FixedLengthHttpResponse(this.cacheFiles ? (byte[])this.cache.get(p.toString()) : Files.readAllBytes(p));
		  String ext = null;
		  if (p.toString().contains("."))
		    ext = p.toString().substring(p.toString().lastIndexOf(".") + 1, p.toString().length()); 
		  response.setContentType((ext != null) ? MimeTypes.getMimeType(ext) : "application/octet-stream");
		  this.log.debug("Deliver file " + p.toString());
      } else {
    	  response=new FixedLengthHttpResponse("not found".getBytes(), 404);
      }
      return response;
    } catch (IOException e) {
      FixedLengthHttpResponse response = new FixedLengthHttpResponse("ERROR".getBytes());
      response.setStatusCode(404);
      return response;
    } 
  }
  
  public boolean keepAlive() {
    return false;
  }
  
  protected StaticFileRequestHandler() {}
}
