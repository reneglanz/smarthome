package de.shd.automation.action;

import de.core.CoreException;
import de.core.serialize.annotation.Element;
import de.core.service.Service;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.shd.automation.Data;
import de.shd.device.State;
import de.shd.device.Switch;
import de.shd.ui.Template;

public class SwitchAction implements Action, Template {
	private static class NotFound implements Switch {
		private NotFound() {
		}

		public State toggle() throws CoreException {
			return State.UNKNOWN;
		}

		public State getState() throws CoreException {
			return State.UNKNOWN;
		}

		public State setState(State state) throws CoreException {
			return State.UNKNOWN;
		}
	}

	private static NotFound NOT_FOUND = new NotFound();

	@Element protected String provider = null;
	@Element protected String service = null;
	@Element protected String state = null;

	protected Switch switch0;

	public void execute(Data data) throws CoreException {
		Switch switch1 = getService();
		State swState=State.UNKNOWN;
		String resolved=resolve(this.state, data);
		try{
			swState=State.valueOf(resolved);
			switch1.setState(swState);
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		} 
	}

	public Switch getService() {
		if (this.switch0 == null) {
			ServiceProvider<? extends Service> serviceprovider = Services.getProvider(this.provider);
			if (serviceprovider != null) {
				try {
					Service service0 = serviceprovider.getService(this.service);
					if (service0 != null && service0 instanceof Switch) {
						this.switch0 = (Switch) service0;
					} else {
						this.switch0 = NOT_FOUND;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			} else {
				this.switch0 = NOT_FOUND;
			}
		}
		return this.switch0;
	}
}
