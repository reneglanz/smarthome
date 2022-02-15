package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class VideoStreamingData implements Data {
	@Element protected String url; 
	@Element protected String contentType;
	
	protected VideoStreamingData() {}
	
	public VideoStreamingData(String value, String contentType) {
		super();
		this.url = value;
		this.contentType = contentType;
	}
}
