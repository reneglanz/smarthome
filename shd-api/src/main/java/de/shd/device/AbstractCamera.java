package de.shd.device;

import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.http.Http;
import de.core.serialize.Coding;
import de.core.serialize.annotation.Element;
import de.core.utils.Streams;
import de.shd.device.data.ImageData;
import de.shd.device.data.StreamData;

public abstract class AbstractCamera extends AbstractDevice implements Camera {

	@Element protected String imageUrl;
	@Element protected String videoUrl;
	@Element protected String videoType;
	
	@Override
	public ExportData createExportData() {
		return new ExportData(getDeviceId(), name);
	}
	
	public String getDataUrl(byte[] image) {
		return "data:image/jpeg;base64,"+Coding.toBase64(image);
	}
	
	public ImageData getImage() throws CoreException{
		try {
			if(imageUrl!=null) {
				Map<String,String> header=new HashMap<>();
				byte[] data=Streams.readAll(Http.get(imageUrl,header).getContent());
				return new ImageData(getDataUrl(data));
			}
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}

	@Override
	public StreamData getStream() throws CoreException {
		return new StreamData(videoUrl, videoType);
	}
}
