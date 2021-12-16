package de.shd.device;

import java.util.ArrayList;
import java.util.List;

import de.core.data.Data;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class ExportData implements Serializable {

	@Element(inline = true, inlineClasses = { NameHandle.class }) protected Handle device;
	@Element protected String deviceName;
	@Element protected List<Data> data = new ArrayList<Data>();
	protected ExportData() {}
	public ExportData(Handle device, String deviceName, Data... data) {
		this.device = device;
		this.deviceName = deviceName;
		for (Data d : data) {
			this.data.add(d);
		}
	}
	public ExportData(Handle device, String deviceName, List<Data> data) {
		this.device = device;
		this.deviceName = deviceName;
		this.data = data;
	}

	public Handle getDeviceHandle() {
		return this.device;
	}

	public List<Data> getData() {
		return this.data;
	}
}
