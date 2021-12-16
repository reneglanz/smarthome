package de.shd.ui;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;

public interface ResourceService extends Service {

	@Function public void update() throws CoreException;
	@Function public ResourceFile get() throws CoreException;
	@Function byte[] getContent(@Param("entry") ResourceFile.Entry entry) throws CoreException;
}