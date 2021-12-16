package de.core.http.mime;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
  private static Map<String, String> mimeTypes = new HashMap<>();
  
  static {
    mimeTypes.put("jpeg", "image/jpeg");
    mimeTypes.put("jpg", "image/jpg");
    mimeTypes.put("svg", "image/svg+xml");
    mimeTypes.put("txt", "plain/text");
    mimeTypes.put("html", "text/html");
    mimeTypes.put("htm", "text/html");
    mimeTypes.put("jar", "application/octet-stream");
    mimeTypes.put("css", "text/css");
    mimeTypes.put("js", "text/javascript");
    mimeTypes.put("json", "application/json");
    mimeTypes.put("sjos", "application/sjos");
    mimeTypes.put("jar", "application/java-archive");
    mimeTypes.put("xml", "text/xml");
  }
  
  public static String getMimeType(String extension) {
    return mimeTypes.get(extension);
  }
}
