package de.core.rt;

import de.core.CoreException;

public interface Releasable {
  void release() throws CoreException;
}
