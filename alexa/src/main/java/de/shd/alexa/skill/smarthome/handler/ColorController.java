package de.shd.alexa.skill.smarthome.handler;

import java.util.HashMap;

import de.core.CoreException;
import de.shd.alexa.skill.smarthome.model.ColorHSB;
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

public class ColorController extends AbstractHandler {
  protected static HashMap<ColorHSB, Light.Color> colorMapping = new HashMap<>();
  
  static {
    colorMapping.put(new ColorHSB(0.0F, 1.0F, 1.0F), new Light.Color(255, 0, 0));
    colorMapping.put(new ColorHSB(348.0F, 0.9087F, 0.8588F), new Light.Color(255, 24, 70));
    colorMapping.put(new ColorHSB(17.0F, 42.0F, 1.0F), new Light.Color(255, 160, 122));
    colorMapping.put(new ColorHSB(39.0F, 1.0F, 1.0F), new Light.Color(255, 166, 0));
    colorMapping.put(new ColorHSB(50.0F, 1.0F, 1.0F), new Light.Color(254, 213, 0));
    colorMapping.put(new ColorHSB(60.0F, 1.0F, 1.0F), new Light.Color(255, 255, 0));
    colorMapping.put(new ColorHSB(120.0F, 1.0F, 1.0F), new Light.Color(0, 255, 0));
    colorMapping.put(new ColorHSB(174.0F, 0.7188F, 0.8784F), new Light.Color(74, 255, 238));
    colorMapping.put(new ColorHSB(180.0F, 1.0F, 1.0F), new Light.Color(0, 255, 255));
    colorMapping.put(new ColorHSB(197.0F, 0.4231F, 0.9176F), new Light.Color(148, 225, 255));
    colorMapping.put(new ColorHSB(240.0F, 1.0F, 1.0F), new Light.Color(0, 0, 255));
    colorMapping.put(new ColorHSB(277.0F, 0.8619F, 0.9373F), new Light.Color(171, 35, 255));
    colorMapping.put(new ColorHSB(300.0F, 1.0F, 1.0F), new Light.Color(255, 0, 255));
    colorMapping.put(new ColorHSB(348.0F, 0.251F, 1.0F), new Light.Color(255, 191, 205));
    colorMapping.put(new ColorHSB(255.0F, 0.5F, 1.0F), new Light.Color(159, 128, 255));
  }
  
  public Response handle(Request request) {
    Header header = request.getDirective().getHeader();
    Endpoint endpoint = request.getDirective().getEndpoint();
    try {
      AbstractDevice abstractDevice = getDeviceStore().getService(endpoint.getEndpointId());
      if (abstractDevice instanceof Light) {
        Light light = (Light)abstractDevice;
        Payload payload = request.getDirective().getPayload();
        if ("SetColor".equals(header.getName())) {
          Light.Color c = colorMapping.get(payload.getColorHSB());
          if (c != null)
            light.setColor(c); 
        } 
        header.setNamespace("Alexa");
        header.setName("Response");
        endpoint.setManufacturerName(null);
        endpoint.setCapabilities(null);
        Event event = new Event(header, endpoint);
        Context context = new Context(new Properties[] { Properties.Factory.colorController(ColorHSB.forColor(light.getColor())), Properties.Factory.brigthnessController(light.getBrightness()) });
        return new Response(event, context);
      } 
    } catch (CoreException e) {
      e.printStackTrace();
    } 
    return null;
  }
}
