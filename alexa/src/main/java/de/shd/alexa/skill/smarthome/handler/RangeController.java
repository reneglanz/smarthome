package de.shd.alexa.skill.smarthome.handler;

import de.core.CoreException;
import de.shd.alexa.skill.smarthome.model.Context;
import de.shd.alexa.skill.smarthome.model.Endpoint;
import de.shd.alexa.skill.smarthome.model.Event;
import de.shd.alexa.skill.smarthome.model.Header;
import de.shd.alexa.skill.smarthome.model.Payload;
import de.shd.alexa.skill.smarthome.model.Properties;
import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;
import de.shd.device.AbstractDevice;
import de.shd.device.Range;
import de.shd.device.Switch;

public class RangeController extends AbstractHandler {

	@Override
	public Response handle(Request request) {
	    Header header = request.getDirective().getHeader();
	    Endpoint endpoint = request.getDirective().getEndpoint();
	    try {
	      AbstractDevice abstractDevice = getDeviceStore().getService(endpoint.getEndpointId());
	      
	     Payload payload = request.getDirective().getPayload();
	     if(abstractDevice instanceof Range&&payload.getRangeValue()>-1) {
	    	 Range range=(Range)abstractDevice;
	    	 range.setRange(payload.getRangeValue());
	     }
        header.setNamespace("Alexa");
        header.setName("Response");
        endpoint.setManufacturerName(null);
        endpoint.setCapabilities(null);
        Event event = new Event(header, endpoint);
        Context context = new Context(Properties.Factory.powerController(((Switch)abstractDevice).getState().toString()));
        return new Response(event, context);
	    } catch (CoreException e) {
	      e.printStackTrace();
	    } 
	    return null;
	}

}
