package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.annotation.Element;

public class RollerRangeControllerProperties extends ControllerProperties {
	@Element protected String instanceo="Blind.Lift";
	
	public RollerRangeControllerProperties(String value) {
		super("Alexa.RangeController", "rangeValue", value);
	}
}
