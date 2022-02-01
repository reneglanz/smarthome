package de.shd.device;

import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.http.Http;
import de.core.serialize.Coding;
import de.core.serialize.annotation.Element;
import de.core.service.Function;
import de.core.utils.Streams;
import de.shd.device.data.ImageData;

public class Camera extends AbstractDevice implements Sensor {

	@Element protected String imageUrl;
	@Element protected String videoUrl;
	@Element(defaultValue="10000") protected int imageRefreshRate=10000;
	
	@Override
	public ExportData createExportData() {
		return new ExportData(getDeviceHandle(), name);
	}
	
	public String getDataUrl(byte[] image) {
		return "data:image/jpeg;base64,"+Coding.toBase64(image);
	}
	
	@Function
	public ImageData getImage() throws CoreException{
		try {
			Map<String,String> header=new HashMap<>();
			header.put("Authorization", "Basic "+Coding.toBase64("admin:admin".getBytes()));
			byte[] data=Streams.readAll(Http.get(imageUrl,header).getContent());
			return new ImageData(getDataUrl(data));
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}
	

//	@Override
//	public void launch() throws CoreException {
//		scheduler.schedule(new IntervalTask(this.imageRefreshRate) {
//			@Override
//			public void execute() throws CoreException {
//				try {
//					lastImage=Http.get(imageUrl);
//					Camera.this.export();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

 
}
