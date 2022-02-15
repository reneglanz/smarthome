package de.shd.alexa.skill.smarthome.handler;

import de.core.CoreException;
import de.shd.alexa.skill.smarthome.model.Context;
import de.shd.alexa.skill.smarthome.model.Endpoint;
import de.shd.alexa.skill.smarthome.model.Event;
import de.shd.alexa.skill.smarthome.model.Header;
import de.shd.alexa.skill.smarthome.model.Properties;
import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;
import de.shd.device.AbstractDevice;
import de.shd.device.Switch;

public class PowerController extends AbstractHandler {
  public static final String TRUN_ON = "TurnOn";
  public static final String TRUN_OFF = "TurnOff";
  
  public Response handle(Request request) {
    Header header = request.getDirective().getHeader();
    Endpoint endpoint = request.getDirective().getEndpoint();
    try {
      AbstractDevice abstractDevice = getDeviceStore().getService(endpoint.getEndpointId());
      if (abstractDevice instanceof Switch) {
        if ("TurnOn".equals(header.getName())) {
          ((Switch)abstractDevice).setState(Switch.State.ON);
        } else {
          ((Switch)abstractDevice).setState(Switch.State.OFF);
        } 
        header.setNamespace("Alexa");
        header.setName("Response");
        endpoint.setManufacturerName(null);
        endpoint.setCapabilities(null);
        Event event = new Event(header, endpoint);
        Context context = new Context(Properties.Factory.powerController(((Switch)abstractDevice).getState().toString()));
        return new Response(event, context);
      } 
    } catch (CoreException e) {
      e.printStackTrace();
    } 
    return null;
  }
}

