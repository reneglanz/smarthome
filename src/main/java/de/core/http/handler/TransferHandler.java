package de.core.http.handler;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TransferHandler extends AbstractHttpRequestHandler {
  Path dir;
  
  public TransferHandler(Path dir) {
    super("/data");
    this.dir = dir;
  }
  
  public HttpResponse handleRequest(HttpRequest request) {
    if ("POST".equals(request.getMethod().toUpperCase()))
      return doPost(request); 
    if ("GET".equals(request.getMethod().toUpperCase()))
      return doGet(request); 
    return new FixedLengthHttpResponse("Forbidden".getBytes(), 403);
  }
  
  private HttpResponse doGet(HttpRequest request) {
    HttpHeader idHeader = request.getHeader("transfer-id");
    if (idHeader != null) {
      Path file = createFile(idHeader.getValue());
      if (Files.exists(file, new java.nio.file.LinkOption[0]));
      return new FixedLengthHttpResponse("File does not exists".getBytes(), 403);
    } 
    return new FixedLengthHttpResponse("Missing transfer-id".getBytes(), 403);
  }
  
  private HttpResponse doPost(HttpRequest request) {
    HttpHeader idHeader = request.getHeader("transfer-id");
    if (idHeader != null) {
      Path file = createFile(idHeader.getValue());
      if (!Files.exists(file, new java.nio.file.LinkOption[0])) {
        try {
          Files.copy(request.getIs(), file, new java.nio.file.CopyOption[0]);
        } catch (IOException e) {
          e.printStackTrace();
        } 
      } else {
        return new FixedLengthHttpResponse("File already uploaded".getBytes(), 403);
      } 
    } 
    return new FixedLengthHttpResponse("Missing transfer-id".getBytes(), 403);
  }
  
  private Path createFile(String id) {
    return Paths.get(this.dir.toString(), new String[] { id + ".data" });
  }
  
  public boolean keepAlive() {
    return false;
  }
}
