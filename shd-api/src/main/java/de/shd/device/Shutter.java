package de.shd.device;

import de.core.CoreException;
import de.core.service.Function;

public interface Shutter extends Range {
  @Function
  void open() throws CoreException;
  
  @Function
  void close() throws CoreException;
  
  @Function
  void stop() throws CoreException;
  
}
