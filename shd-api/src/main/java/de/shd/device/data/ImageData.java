package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class ImageData implements Data {
	@Element String value; 
	
	protected ImageData() {}

	public ImageData(String value) {
		super();
		this.value = value;
	}
}
