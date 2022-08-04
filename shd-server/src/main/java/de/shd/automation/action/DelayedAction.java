package de.shd.automation.action;

import de.core.CoreException;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.task.DelayedTask;
import de.core.task.Scheduler;
import de.shd.automation.Data;

public class DelayedAction extends DelayedTask implements Action {

	@Element protected int delay;
	@Element protected Action action;
	@Injectable Scheduler scheduler;
	
	private Data parmData;
	
	@Override
	public void execute() throws CoreException {
		this.finished=true;
		action.execute(parmData);
	}

	@Override
	public void execute(Data paramData) throws CoreException {
		this.parmData=paramData;
		scheduler.schedule(this);
	}

	@Override
	public int getDelay() {
		return Integer.parseInt(parmData.get("delay", String.class, Integer.toString(delay)));
	}

	
}
