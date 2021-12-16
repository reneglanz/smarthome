package de.shd.alexa.skill.custom;

import de.core.handle.NameHandle;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.log.Logger;
import de.core.serialize.SJOSDeserializer;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Root;
import de.core.serialize.parser.CodingReadHandler;
import de.core.serialize.parser.DefaultReadHandler;
import de.core.serialize.parser.JsonParser;
import de.core.utils.Streams;
import de.shd.alexa.skill.custom.handler.DimIntentHandler;
import de.shd.alexa.skill.custom.handler.IntentHandler;
import de.shd.alexa.skill.custom.handler.LaunchIntentHandler;
import de.shd.alexa.skill.custom.handler.SetBrightnessIntentHandler;
import de.shd.alexa.skill.custom.handler.StopIntentHandler;
import de.shd.alexa.skill.custom.handler.SwitchIntentHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AlexaRequestHandler extends AbstractHttpRequestHandler {
  JsonParser parser = new JsonParser();
  
  List<IntentHandler> handler = new ArrayList<>();
  
  SJOSDeserializer desiralizer = new SJOSDeserializer();
  
  Logger logger = Logger.createLogger("AlexaRequestHandler");
  
  protected NameHandle deviceProvider = new NameHandle("devices");
  
  protected AlexaRequestHandler() {
    this.handler.add(new LaunchIntentHandler());
    this.handler.add(new SwitchIntentHandler(this.deviceProvider));
    this.handler.add(new StopIntentHandler());
    this.handler.add(new SetBrightnessIntentHandler(this.deviceProvider));
    this.handler.add(new DimIntentHandler(this.deviceProvider));
  }
  
  public HttpResponse handleRequest(HttpRequest request) {
    try {
      if (validateRequest(request)) {
        int contentLength = -1;
        HttpHeader header = request.getHeader("Content-Length");
        if (header != null && header.getValue() != null && header.getValue().length() > 0)
          contentLength = Integer.parseInt(header.getValue()); 
        DefaultReadHandler handler = new DefaultReadHandler();
        if (contentLength > 0) {
          this.parser.parse(new BufferedReader(new InputStreamReader(request.getIs())), (CodingReadHandler)handler);
          Root root = handler.getResult();
          ComplexElement request0 = (ComplexElement)root.getChild("request");
          if (request0 != null) {
            Root tmp = new Root();
            tmp.getChildren().addAll(request0.getChildren());
            AlexaRequest alexaRequest = (AlexaRequest)this.desiralizer.deserialze((ComplexElement)tmp, AlexaRequest.class);
            this.logger.debug("Intent: " + alexaRequest.getIntentName());
            IntentHandler intentHandler = getHandler(alexaRequest);
            if (intentHandler != null) {
              this.logger.debug("Intent: " + alexaRequest.getSlot("device", ""));
              AlexaResponse alexaResppnse = intentHandler.handle(alexaRequest);
              FixedLengthHttpResponse fixedLengthHttpResponse = new FixedLengthHttpResponse(alexaResppnse.getBytes());
              fixedLengthHttpResponse.addHeader(new HttpHeader("Content-Type", "application/json"));
              return (HttpResponse)fixedLengthHttpResponse;
            } 
          } 
          FixedLengthHttpResponse resp = new FixedLengthHttpResponse((new AlexaResponse()).speak("Ich weiß nicht was du meints").getBytes());
          resp.addHeader(new HttpHeader("Content-Type", "application/json"));
          return (HttpResponse)resp;
        } 
      } 
      return (HttpResponse)new FixedLengthHttpResponse("UNKNOWN REQUEST".getBytes(), 404);
    } catch (Exception ex) {
      ex.printStackTrace();
      return (HttpResponse)new FixedLengthHttpResponse("ERROR".getBytes(), 500);
    } 
  }
  
  public boolean validateRequest(HttpRequest request) {
    return true;
  }
  
  public boolean keepAlive() {
    return false;
  }
  
  protected IntentHandler getHandler(AlexaRequest request) {
    for (IntentHandler h : this.handler) {
      if (h.canHandle(request))
        return h; 
    } 
    return null;
  }
}
