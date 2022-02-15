package de.shd.ui;

import java.util.List;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;

public interface Editable extends Service {
	@Function public List<String> list() throws CoreException;
	@Function public String get(@Param("handle") String handle) throws CoreException;
	@Function public void update(@Param("content") String content) throws CoreException;
	@Function public void add(@Param("content") String content) throws CoreException;
	@Function public String template(@Param("name") String name) throws CoreException;
}
