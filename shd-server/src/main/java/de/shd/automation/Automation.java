package de.shd.automation;

import java.util.ArrayList;
import java.util.List;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.automation.action.Action;
import de.shd.automation.condition.Condition;
import de.shd.automation.trigger.AbstractTrigger;

public class Automation implements Serializable, Launchable, Releasable {
	Logger log=Logger.createLogger("Automation");
	
	@Element String id;
	@Element List<AbstractTrigger> trigger;
	@Element List<Condition> conditions;
	@Element(name = "than") List<Action> than0;
	@Element(name = "else") List<Action> else0;

	protected String file;

	public static class Builder {
		private Automation automation = new Automation();

		public Builder addTrigger(AbstractTrigger trigger) {
			if (this.automation.trigger == null)
				this.automation.trigger = new ArrayList<>();
			this.automation.trigger.add(trigger);
			trigger.setAutomation(this.automation);
			return this;
		}

		public Builder addCondtion(Condition condition) {
			this.automation.conditions.add(condition);
			return this;
		}

		public Builder addThan(Action _than) {
			if (this.automation.than0 == null)
				this.automation.than0 = new ArrayList<>();
			this.automation.than0.add(_than);
			return this;
		}

		public Builder addElse(Action _else) {
			if (this.automation.else0 == null)
				this.automation.else0 = new ArrayList<>();
			this.automation.else0.add(_else);
			return this;
		}

		public Builder setId(String id) {
			this.automation.id = id;
			return this;
		}

		public Automation build() {
			return this.automation;
		}
	}

	protected Data data = new Data();

	private Automation() {
		this.conditions = new ArrayList<>();
	}

	public void run() throws CoreException {
		boolean result = true;
		this.data.set("datetime", "" + System.currentTimeMillis());
		if (this.conditions != null)
			for (int i = 0; i < this.conditions.size(); i++) {
				if (i > 0) {
					switch (((Condition) this.conditions.get(i)).getOperator()) {
					case AND:
						result = (result && ((Condition) this.conditions.get(i)).resolve(this.data));
					case OR:
						result = (result || ((Condition) this.conditions.get(i)).resolve(this.data));
						break;
					}
				} else {
					result = ((Condition) this.conditions.get(0)).resolve(this.data);
				}
			}
		if (result) {
			if (this.than0 != null)
				for (Action a : this.than0) {
					try {
						a.execute(this.data);
					} catch (Exception e) {
						log.error("["+this.id +"] Failed to execute action "+a.toString());
					}
				}
		} else if (this.else0 != null) {
			for (Action a : this.else0) {
				try {
					a.execute(this.data);
				} catch (Exception e) {
					log.error("["+this.id +"] Failed to execute action "+a.toString());
				}
			}
		}
	}

	public String getId() {
		return this.id;
	}

	public void launch() throws CoreException {
		for (AbstractTrigger trigger : this.trigger) {
			trigger.setAutomation(this);
			if (trigger instanceof Launchable)
				((Launchable) trigger).launch();
		}
	}

	public Data getData() {
		return this.data;
	}

	public void release() throws CoreException {
		release(this.trigger);
		release(this.else0);
		release(this.than0);
		release(this.conditions);
	}

	public void release(List<?> list) {
		if (list != null)
			for (Object o : list) {
				if (o instanceof Releasable)
					try {
						((Releasable) o).release();
					} catch (CoreException coreException) {
					}
			}
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String toString() {
		return id;
	}
}
