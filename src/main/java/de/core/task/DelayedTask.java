package de.core.task;

import de.core.CoreException;

public abstract class DelayedTask implements Task {

	protected boolean finished=false;
	protected String id;

	@Override
	public long next() throws CoreException {
		return System.currentTimeMillis()+getDelay();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean finished() {
		return finished;
	}
	
	public abstract int getDelay();
	
}
