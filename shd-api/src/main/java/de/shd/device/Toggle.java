package de.shd.device;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Service;

public interface Toggle extends Service {
  @Function
  Switch.State toggle() throws CoreException;
}
