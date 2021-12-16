package de.shd.device;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;

public interface Range extends Service {

	@Function public void setRange(@Param("value") int value) throws CoreException;
	@Function public int getRange() throws CoreException;
}
