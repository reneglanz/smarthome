package de.shd.device.data;

import de.core.data.Data;
import de.core.serialize.annotation.Element;

public class StreamData implements Data {
	@Element String streamUrl;
	@Element String videoType;
	
	protected StreamData() {}
	public StreamData(String streamUrl, String videoType) {
		super();
		this.streamUrl = streamUrl;
		this.videoType = videoType;
	}
}
