package de.shd.device;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.service.Castable;
import de.core.service.Service;
import de.core.service.Services;
import de.shd.update.UpdateService;

public abstract class AbstractDevice implements Service, Serializable, Castable {
	@Element protected String id;
	@Element protected String name;
	@Element(defaultValue = "false") protected boolean discoverable = false;
	@Element protected String alexaType;
	@Element protected String alexaName;
	
	protected UpdateService updateService;

	protected AbstractDevice() {
	}

	public AbstractDevice(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String getDeviceId() {
		return this.id;
	}

	public abstract ExportData createExportData();

	public void export() throws CoreException {
		ExportData data = createExportData();
		if (data != null) {
			if (this.updateService == null)
				this.updateService = (UpdateService) Services.get(UpdateService.class);
			if (this.updateService != null)
				this.updateService.update(data);
		}
	}

	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	public String getServiceHandle() {
		return this.id;
	}

	public boolean isDiscoverable() {
		return this.discoverable;
	}

	public String getAlexaType(String default0) {
		return alexaType!=null?alexaType:default0;
	}

	public String getAlexaName() {
		return alexaName;
	}
	
	
}
