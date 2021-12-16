package de.shd.device;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;

public interface Switch extends Toggle, Service {
  @Function
  State getState() throws CoreException;
  
  @Function
  State setState(@Param("state") State paramState) throws CoreException;
  
  public enum State {
    ON, OFF, UNKNOWN;
  }
}
