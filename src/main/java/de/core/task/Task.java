package de.core.task;

import de.core.CoreException;

public interface Task {
  void execute() throws CoreException;
  
  long next(long paramLong) throws CoreException;
  
  String getId();
}
