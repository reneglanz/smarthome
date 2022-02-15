package de.shd.automation.action;

import de.core.CoreException;
import de.core.serialize.annotation.Element;
import de.core.service.Service;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.shd.automation.Data;
import de.shd.device.Switch;
import de.shd.device.Switch.State;
import de.shd.ui.Template;

public class SwitchAction implements Action, Template {
	private static class NotFound implements Switch {
		private NotFound() {
		}

		public Switch.State toggle() throws CoreException {
			return Switch.State.UNKNOWN;
		}

		public Switch.State getState() throws CoreException {
			return Switch.State.UNKNOWN;
		}

		public Switch.State setState(Switch.State state) throws CoreException {
			return Switch.State.UNKNOWN;
		}
	}

	private static NotFound NOT_FOUND = new NotFound();

	@Element(inline = true)
	protected String provider = null;

	@Element(inline = true)
	protected String service = null;

	@Element
	protected Switch.State state;

	@Element
	protected String key;

	protected Switch switch0;

	public void execute(Data data) throws CoreException {
		Switch switch1 = getService();
		if (state != null && state != State.UNKNOWN) {
			switch1.setState(this.state);
		} else if (key != null) {
			String value = data.get(key, String.class, null);
			if (value != null)
				try {
					State tmpState = State.valueOf(value);
					switch1.setState(tmpState);
				} catch (Exception e) {
					CoreException.throwCoreException("Invalid value " + value);
				}
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

	public static void main(String[] args) throws CoreException {
		System.out.println(new SwitchAction().tempalte());
	}
}
