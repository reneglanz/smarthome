package de.shd.alexa.skill.smarthome.handler;

import java.util.ArrayList;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.serialize.annotation.Injectable;
import de.core.service.Function;
import de.core.service.Service;
import de.shd.alexa.skill.smarthome.Capability;
import de.shd.alexa.skill.smarthome.model.DiscoverPayload;
import de.shd.alexa.skill.smarthome.model.Endpoint;
import de.shd.alexa.skill.smarthome.model.Event;
import de.shd.alexa.skill.smarthome.model.EventPayload;
import de.shd.alexa.skill.smarthome.model.Header;
import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;
import de.shd.device.AbstractDevice;
import de.shd.device.DeviceProvider;
import de.shd.device.Light;
import de.shd.device.Range;

public class DiscoverHandler extends AbstractHandler implements Service {
  @Injectable
  DeviceProvider deviceStore;
  Logger logger=Logger.createLogger("DiscoverHandler");
  
  protected long visible = -1L;
  
  public Response handle(Request request) {
    ArrayList<Endpoint> endpoints = new ArrayList<>();
    if (this.visible + 60000L > System.currentTimeMillis())
      for (AbstractDevice device : getDeviceStore()) {
        if (device.isDiscoverable()) {
          Endpoint endpoint = new Endpoint();
          endpoint.setEndpointId(device.getServiceHandle().toString());
          endpoint.setFriendlyName(device.getAlexaName()!=null?device.getAlexaName():device.getName());
          endpoint.setDescription(device.getName());
          ArrayList<Capability> capabillities = new ArrayList<>();
          capabillities.add(Capability.Factory.createAlexa());
          if (!(device instanceof de.shd.device.Switch) 
        	  && !(device instanceof Light)
        	  && !(device instanceof Range))
            continue; 
          if (device instanceof de.shd.device.Switch) {
            capabillities.add(Capability.Factory.createPowerController());
           	endpoint.setDisplayCategories(new String[] { device.getAlexaType("SWITCH") });
          } 
          if (device instanceof Light) {
            Light light = (Light)device;
            endpoint.setDisplayCategories(new String[] { device.getAlexaType("LIGHT") });
            try {
              if (light.canColor())
                capabillities.add(Capability.Factory.createColorController()); 
              if (light.canDim())
                capabillities.add(Capability.Factory.createBrightnessController()); 
              if (light.canColorTemp())
                capabillities.add(Capability.Factory.createColorTempreture()); 
            } catch (CoreException coreException) {}
          } 
          if(device instanceof Range) {
        	  endpoint.setDescription("INTERIOR_BLIND");
        	  capabillities.add(Capability.Factory.createRollerRange());
          }
          endpoint.setCapabilities(capabillities.<Capability>toArray(new Capability[capabillities.size()]));
          endpoints.add(endpoint);
        } 
      }  
    Header header = request.getDirective().getHeader();
    header.setName("Discover.Response");
    return new Response(new Event(header, (EventPayload)new DiscoverPayload(endpoints.<Endpoint>toArray(new Endpoint[endpoints.size()]))));
  }
  
  public String getServiceHandle() {
    return "Alexa.Discover";
  }
  
  @Function
  public void visible() throws CoreException {
    this.visible = System.currentTimeMillis();
  }
}
