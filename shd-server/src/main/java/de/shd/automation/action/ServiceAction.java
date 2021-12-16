package de.shd.automation.action;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.serialize.annotation.Element;
import de.core.service.Call;
import de.core.service.Services;
import de.shd.automation.Data;
import java.util.HashMap;
import java.util.Map;

public class ServiceAction implements Action {
  @Element(inline = true)
  NameHandle provider;
  
  @Element(inline = true)
  NameHandle service;
  
  @Element
  String method;
  
  @Element
  protected Map<String, Object> parameter = new HashMap<>();
  
  public void execute(Data data) throws CoreException {
    data.getData().putAll(this.parameter);
    Services.invoke(new Call((Handle)this.provider, (Handle)this.service, this.method, data.getData()));
  }
}
