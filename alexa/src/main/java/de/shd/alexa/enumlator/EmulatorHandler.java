package de.shd.alexa.enumlator;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.http.mime.MimeTypes;
import de.core.log.Logger;
import de.core.utils.Streams;

public class EmulatorHandler extends AbstractHttpRequestHandler {
  Logger logger = Logger.createLogger("WemoEmulator");
  
  public HttpResponse handleRequest(HttpRequest request) {
    try {
      String data = null;
      if (request.getRequestPath() != null && request.getRequestPath().endsWith("/setup.xml")) {
        data = new String(Streams.readAll(getClass().getResourceAsStream("/emulator/setup.xml")));
        this.logger.debug("send setup.xml");
      } else if (request.getRequestPath() != null && request.getRequestPath().endsWith("/eventservice.xml")) {
        this.logger.debug("send eventservice.xml");
        data = new String(Streams.readAll(getClass().getResourceAsStream("/emulator/eventservice.xml")));
      } 
      if (data != null) {
        data = data.replace("${device.uuid}", "TestLampe").replace("${device.name}", "TestLampe");
        FixedLengthHttpResponse resp = new FixedLengthHttpResponse(data.getBytes());
        resp.addHeader(new HttpHeader("Content-Type", MimeTypes.getMimeType("xml")));
        return (HttpResponse)resp;
      } 
      return (HttpResponse)new FixedLengthHttpResponse("not found".getBytes(), 404);
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    } 
  }
  
  public boolean keepAlive() {
    return false;
  }
}
