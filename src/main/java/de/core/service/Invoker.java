package de.core.service;

import de.core.CoreException;

public interface Invoker {
  <E> E invoke(Call paramCall) throws CoreException;
}
