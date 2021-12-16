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

public class ColorTempratureController extends AbstractHandler {
  public int[] temp = new int[] { 3000, 4750, 6465 };
  
  public Response handle(Request request) {
    Header header = request.getDirective().getHeader();
    Endpoint endpoint = request.getDirective().getEndpoint();
    try {
      AbstractDevice abstractDevice = getDeviceStore().getService((Handle)new NameHandle(endpoint.getEndpointId()));
      if (abstractDevice instanceof Light) {
        Light light = (Light)abstractDevice;
        Payload payload = request.getDirective().getPayload();
        if ("SetColorTemperature".equals(header.getName())) {
          int x = getActColorTemp(payload.getColorTemperatureInKelvin());
          light.setColorTemp(x);
        } else if ("IncreaseColorTemperature".equals(header.getName())) {
          int x = getActColorTemp(light.getColorTemp());
          light.setBrightness((x + 1 < this.temp.length) ? this.temp[x + 1] : light.getColorTemp());
        } else if ("DecreaseColorTemperature".equals(header.getName())) {
          int x = getActColorTemp(light.getColorTemp());
          light.setBrightness((x - 1 >= 0) ? this.temp[x - 1] : light.getColorTemp());
        } 
        header.setNamespace("Alexa");
        header.setName("Response");
        endpoint.setManufacturerName(null);
        endpoint.setCapabilities(null);
        Event event = new Event(header, endpoint);
        Context context = new Context(Properties.Factory.colorTempController(light.getColorTemp()));
        return new Response(event, context);
      } 
    } catch (CoreException e) {
      e.printStackTrace();
    } 
    return null;
  }
  
  public int getActColorTemp(int acttemp) {
    int bestIdx = -1;
    int bestDiff = Integer.MAX_VALUE;
    for (int i = 0; i < this.temp.length; i++) {
      if (this.temp[i] == acttemp)
        return i; 
      int diff = acttemp - this.temp[i];
      if (diff < 0)
        diff *= -1; 
      if (diff < bestDiff) {
        bestDiff = diff;
        bestIdx = i;
      } 
    } 
    return bestIdx;
  }
}
