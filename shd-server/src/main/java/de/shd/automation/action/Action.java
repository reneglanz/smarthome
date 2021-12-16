package de.shd.automation.action;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.shd.automation.Data;

public interface Action extends Serializable {
  void execute(Data paramData) throws CoreException;
}
