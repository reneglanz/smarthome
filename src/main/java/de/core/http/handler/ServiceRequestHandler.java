package de.core.http.handler;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.mime.MimeTypes;
import de.core.serialize.Coding;
import de.core.service.Call;
import de.core.service.CallResponse;
import de.core.service.Services;
import java.io.ByteArrayOutputStream;

public class ServiceRequestHandler extends AbstractHttpRequestHandler {
  public ServiceRequestHandler() {
    super("/service");
  }
  
  public HttpResponse handleRequest(HttpRequest request) {
    FixedLengthHttpResponse resp;
    HttpHeader contentType = request.getHeader("Content-Type");
    String type = "sjos";
    if (contentType.getValue().startsWith(MimeTypes.getMimeType("json")))
      type = "json"; 
    try {
      Call call = (Call)Coding.decode(request.getIs(), type);
      Object result = Services.invoke(call);
      if (result != null) {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        Coding.encode(CallResponse.create(result), boas, type);
        resp = new FixedLengthHttpResponse(boas.toByteArray());
        resp.setContentType("application/json");
      } else {
        resp = new FixedLengthHttpResponse(new byte[0]);
      } 
      resp.setStatusCode(200);
    } catch (Throwable t) {
      try {
        resp = new FixedLengthHttpResponse(Coding.encode(CallResponse.create(t), type));
        resp.setStatusCode(200);
      } catch (Throwable t2) {
        resp = new FixedLengthHttpResponse("error".getBytes());
        resp.setStatusCode(500);
      } 
    } 
    return resp;
  }
  
  public boolean keepAlive() {
    return false;
  }
}
