package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.automation.Automation;
import de.shd.automation.Data;

public abstract class AbstractTrigger implements Serializable {
	Logger logger = Logger.createLogger("Trigger");
	@Element protected Data data;
	
	protected Automation automation;

	public void setAutomation(Automation automation) {
		this.automation = automation;
	}

	public void runAutomation() throws CoreException {
		if (this.automation == null)
			CoreException.throwCoreException("Automation not set");
		this.automation.getData().merge(data);
		this.automation.getData().set("date", Long.toString(System.currentTimeMillis()));
		logger.info("Automation " + this.automation.getId() + " triggered "+"["+this.getClass().getName()+"]");
		this.automation.run();
	}
}
