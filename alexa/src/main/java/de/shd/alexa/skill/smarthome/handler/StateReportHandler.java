package de.shd.alexa.skill.smarthome.handler;

import java.util.ArrayList;

import de.core.CoreException;
import de.shd.alexa.skill.smarthome.model.ColorHSB;
import de.shd.alexa.skill.smarthome.model.Context;
import de.shd.alexa.skill.smarthome.model.Endpoint;
import de.shd.alexa.skill.smarthome.model.Event;
import de.shd.alexa.skill.smarthome.model.Header;
import de.shd.alexa.skill.smarthome.model.Properties;
import de.shd.alexa.skill.smarthome.model.Request;
import de.shd.alexa.skill.smarthome.model.Response;
import de.shd.device.AbstractDevice;
import de.shd.device.Light;
import de.shd.device.Range;
import de.shd.device.Switch;

public class StateReportHandler extends AbstractHandler {
	public Response handle(Request request) {
		Endpoint endpoint = request.getDirective().getEndpoint();
		Header header = request.getDirective().getHeader();
		try {
			AbstractDevice device = getDeviceStore().getService(endpoint.getEndpointId());
			header.setName("StateReport");
			Event event = new Event(header, endpoint);
			ArrayList<Properties> props = new ArrayList<>();
			if(device instanceof Switch) {
				props.add(Properties.Factory.powerController(((Switch) device).getState().toString()));
			}
			if(device instanceof Light) {
				Light light = (Light) device;
				if (light.canDim()) {
					props.add(Properties.Factory.brigthnessController(light.getBrightness()));
				} else if (light.canColorTemp()) {
					props.add(Properties.Factory.colorTempController(light.getColorTemp()));
				} else if (light.canColor()) {
					props.add(Properties.Factory.colorController(ColorHSB.forColor(light.getColor())));
				}
			}
			if(device instanceof Range) {
				props.add(Properties.Factory.rollerRangeController(((Range) device).getRange()));
			}
			Response response = new Response(event,
					new Context(props.<Properties>toArray(new Properties[props.size()])));
			return response;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
}
