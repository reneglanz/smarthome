package de.shd.device;

import java.util.ArrayList;
import java.util.List;

import de.core.data.Data;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class ExportData implements Serializable {

	@Element protected String device;
	@Element protected String deviceName;
	@Element protected List<Data> data = new ArrayList<Data>();
	protected ExportData() {}
	public ExportData(String device, String deviceName, Data... data) {
		this.device = device;
		this.deviceName = deviceName;
		for (Data d : data) {
			if(d!=null) {
				this.data.add(d);
			}
		}
	}
	public ExportData(String device, String deviceName, List<Data> data) {
		this.device = device;
		this.deviceName = deviceName;
		this.data = data;
	}

	public String getDeviceHandle() {
		return this.device;
	}

	public List<Data> getData() {
		return this.data;
	}
	
	public ExportData addData(Data data) {
		this.data.add(data);
		return this;
	}
}
