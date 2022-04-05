package de.shd.automation.action;

import de.core.CoreException;
import de.core.serialize.Coding;
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
	@Element protected State state;
	@Element protected String key;

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
		SwitchAction a=new SwitchAction();
		a.key="trigger:payload";
		a.provider="devives";
		a.service="schriebtisch";
		System.out.println(new String(Coding.encode(a)));
	}
}
