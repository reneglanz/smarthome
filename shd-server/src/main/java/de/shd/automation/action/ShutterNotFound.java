package de.shd.automation.action;

import de.core.CoreException;
import de.shd.device.Shutter;

public class ShutterNotFound implements Shutter {

	@Override
	public void setRange(int value) throws CoreException {
	}

	@Override
	public void open() throws CoreException {
	}

	@Override
	public void close() throws CoreException {
	}

	@Override
	public void stop() throws CoreException {
	}

	@Override
	public int getRange() throws CoreException {
		return 0;
	}
}
