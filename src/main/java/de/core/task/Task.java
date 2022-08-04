package de.core.task;

import de.core.CoreException;

public interface Task {
  void execute() throws CoreException;
  long next() throws CoreException;
  String getId();
  default boolean finished() {
	  return false;
  }
}
