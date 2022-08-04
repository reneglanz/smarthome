package de.shd.automation.action;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.shd.automation.Data;

public interface Action extends Serializable {
  void execute(Data paramData) throws CoreException;
  public default String resolve(String key,Data data) {
	  if(key.startsWith("$")&&key.length()>1) {
		return data.get(key.substring(1), String.class, key);
	  } else {
		return key;
	  }
  }
}
