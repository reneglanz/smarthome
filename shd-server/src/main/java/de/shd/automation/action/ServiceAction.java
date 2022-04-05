package de.shd.automation.action;

import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.serialize.annotation.Element;
import de.core.service.Call;
import de.core.service.Services;
import de.shd.automation.Data;

public class ServiceAction implements Action {
  @Element String provider;
  @Element String service;
  @Element String method;
  @Element protected Map<String, Object> parameter = new HashMap<>();
  
  public void execute(Data data) throws CoreException {
    data.getData().putAll(this.parameter);
    Services.invoke(new Call(this.provider, this.service, this.method, data.getData()));
  }
}
