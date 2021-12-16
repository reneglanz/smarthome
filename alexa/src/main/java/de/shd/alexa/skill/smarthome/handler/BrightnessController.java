package de.shd.alexa.skill.smarthome.handler;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.shd.alexa.skill.smarthome.model.Context;
import de.shd.alexa.skill.smarthome.model.Endpoint;
import de.shd.alexa.skill.smarthome.model.Event;
import de.shd.alexa.skill.smarthome.model.Header;
import de.shd.alexa.skill.smarthome.model.Payload;
import de.shd.alexa.skill.smarthome.model.Properties;
import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;
import de.shd.device.AbstractDevice;
import de.shd.device.Light;

public class BrightnessController extends AbstractHandler {
  public Response handle(Request request) {
    Header header = request.getDirective().getHeader();
    Endpoint endpoint = request.getDirective().getEndpoint();
    try {
      AbstractDevice abstractDevice = getDeviceStore().getService((Handle)new NameHandle(endpoint.getEndpointId()));
      if (abstractDevice instanceof Light) {
        Light light = (Light)abstractDevice;
        Payload payload = request.getDirective().getPayload();
        if ("SetBrightness".equals(header.getName())) {
          light.setBrightness(payload.getBrightness());
        } else if ("AdjustBrightness".equals(header.getName())) {
          light.setBrightness(light.getBrightness() + payload.getBrightness());
        } 
        header.setNamespace("Alexa");
        header.setName("Response");
        endpoint.setManufacturerName(null);
        endpoint.setCapabilities(null);
        Event event = new Event(header, endpoint);
        Context context = new Context(Properties.Factory.brigthnessController(light.getBrightness()));
        return new Response(event, context);
      } 
    } catch (CoreException e) {
      e.printStackTrace();
    } 
    return null;
  }
}
