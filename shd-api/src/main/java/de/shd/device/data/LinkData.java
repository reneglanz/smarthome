package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class LinkData implements Data {
	@Element protected String value;
	protected LinkData() {}
	public LinkData(String link) {
		this.value=link;
	}
}
