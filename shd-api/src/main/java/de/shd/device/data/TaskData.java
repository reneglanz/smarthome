package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class TaskData implements Data {
	@Element protected int interval;
	@Element protected String urn;

	public TaskData() {}
	
	public TaskData(int interval, String urn) {
		super();
		this.interval = interval;
		this.urn = urn;
	}

}
