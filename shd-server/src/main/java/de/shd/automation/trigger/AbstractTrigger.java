package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.automation.Automation;

public abstract class AbstractTrigger implements Serializable {
	Logger logger = Logger.createLogger("Trigger");

	@Element protected String name;
	@Element protected String payload;
	protected Automation automation;

	public void setAutomation(Automation automation) {
		this.automation = automation;
	}

	public void runAutomation() throws CoreException {
		if (this.automation == null)
			CoreException.throwCoreException("Automation not set");
		logger.debug("Automation " + this.automation.getId() + " triggered by " + name+"["+this.getClass().getName()+"]");
		this.automation.run();
	}

	public AbstractTrigger setPayload(String payload) {
		this.payload = payload;
		return this;
	}

	public String dataKey() {
		return ((this.name != null && this.name.length() > 0) ? (this.name + ":") : "") + "payload";
	}

	public Object data(Object defaultValue) {
		return (this.payload != null && this.payload.length() > 0) ? this.payload : defaultValue;
	}
}
