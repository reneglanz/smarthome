package de.core.rt;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Service;

public interface Reloadable extends Service {
  @Function
  void reload() throws CoreException;
}
