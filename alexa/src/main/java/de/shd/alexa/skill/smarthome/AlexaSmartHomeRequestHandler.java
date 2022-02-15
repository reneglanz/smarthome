package de.shd.alexa.skill.smarthome;

import java.util.HashMap;

import de.core.CoreException;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.serialize.Coding;
import de.core.service.Service;
import de.core.service.Services;
import de.shd.alexa.skill.smarthome.handler.BrightnessController;
import de.shd.alexa.skill.smarthome.handler.ColorController;
import de.shd.alexa.skill.smarthome.handler.ColorTempratureController;
import de.shd.alexa.skill.smarthome.handler.DiscoverHandler;
import de.shd.alexa.skill.smarthome.handler.Handler;
import de.shd.alexa.skill.smarthome.handler.PowerController;
import de.shd.alexa.skill.smarthome.handler.RangeController;
import de.shd.alexa.skill.smarthome.handler.StateReportHandler;
import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;

public class AlexaSmartHomeRequestHandler extends AbstractHttpRequestHandler {
  private static HashMap<String, Handler> handler = new HashMap<>();
  
  static {
    handler.put("Alexa.Discovery", new DiscoverHandler());
    handler.put("Alexa.PowerController", new PowerController());
    handler.put("Alexa.BrightnessController", new BrightnessController());
    handler.put("Alexa.ReportState", new StateReportHandler());
    handler.put("Alexa.ColorController", new ColorController());
    handler.put("Alexa.ColorTemperatureController", new ColorTempratureController());
    handler.put("Alexa.RangeController", new RangeController());
  }
 
  public HttpResponse handleRequest(HttpRequest request) {
    try {
      Request alexarequest = (Request)Coding.decode(request.getIs(), "json", Request.class);
      Handler requestHandler = handler.get(alexarequest.getDirective().getHeader().getNamespace());
      if (requestHandler == null)
        requestHandler = handler.get(alexarequest.getDirective().getHeader().getNamespace() + "." + alexarequest.getDirective().getHeader().getName()); 
      if (requestHandler != null) {
        Response alexaresp = requestHandler.handle(alexarequest);
        if (alexaresp != null) {
          FixedLengthHttpResponse resp = new FixedLengthHttpResponse(Coding.encode(alexaresp, "json", true));
          resp.addHeader(new HttpHeader("Content-Type", "application/json"));
          return (HttpResponse)resp;
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return (HttpResponse)new FixedLengthHttpResponse("ich wei� nicht was das soll".getBytes());
  }
  
  public boolean canHandleRequest(HttpRequest request) {
	  HttpHeader header=request.getHeader("auth");
	  if(header!=null&&header.getValue().equals("44e0c278df54ebea483caac2ac5cea695c7785180a7ad30fb8cd4551b99ac6e4")&& super.canHandleRequest(request)) {
		  return true;
	  } else {
		  return false;
	  }
  }
  
  public boolean keepAlive() {
    return false;
  }
  
  public void finish() {
    try {
      Services.bind((Service)handler.get("Alexa.Discovery"));
    } catch (CoreException coreException) {}
  }
}
