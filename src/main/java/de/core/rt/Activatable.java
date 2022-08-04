package de.core.rt;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;

public interface Activatable extends Service {

	@Function public void activate(@Param("serviceId") String serviceId) throws CoreException;
	@Function public void deactivate(@Param("serviceId")String serviceId) throws CoreException;
}
