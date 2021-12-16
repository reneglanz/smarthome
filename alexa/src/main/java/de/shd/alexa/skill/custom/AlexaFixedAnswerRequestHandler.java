package de.shd.alexa.skill.custom;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.log.Logger;
import de.core.serialize.SJOSDeserializer;
import de.core.serialize.annotation.Element;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Root;
import de.core.serialize.parser.CodingReadHandler;
import de.core.serialize.parser.DefaultReadHandler;
import de.core.serialize.parser.JsonParser;
import de.core.utils.Streams;
import de.shd.alexa.skill.custom.handler.LaunchIntentHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class AlexaFixedAnswerRequestHandler extends AbstractHttpRequestHandler {
  @Element
  String skillId;
  
  @Element
  String answer = "Palim Palim";
  
  private JsonParser parser = new JsonParser();
  
  private SJOSDeserializer desiralizer = new SJOSDeserializer();
  
  private Logger logger = Logger.createLogger("AlexaFixedAnswerRequestHandler");
  
  private LaunchIntentHandler launchHandler = new LaunchIntentHandler();
  
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
            AlexaResponse alexaResponse;
            Root tmp = new Root();
            tmp.getChildren().addAll(request0.getChildren());
            AlexaRequest alexaRequest = (AlexaRequest)this.desiralizer.deserialze((ComplexElement)tmp, AlexaRequest.class);
            this.logger.debug("Intent: " + alexaRequest.getIntentName());
            if (this.launchHandler.canHandle(alexaRequest)) {
              alexaResponse = this.launchHandler.handle(alexaRequest);
            } else {
              alexaResponse = (new AlexaResponse()).speak(this.answer);
            } 
            FixedLengthHttpResponse fixedLengthHttpResponse = new FixedLengthHttpResponse(alexaResponse.getBytes());
            fixedLengthHttpResponse.addHeader(new HttpHeader("Content-Type", "application/json"));
            return (HttpResponse)fixedLengthHttpResponse;
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
  
  private boolean validateRequest(HttpRequest request) {
    return true;
  }
  
  public boolean keepAlive() {
    return false;
  }
}
