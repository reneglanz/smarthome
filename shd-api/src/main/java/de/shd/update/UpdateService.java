package de.shd.update;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.service.Call;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;
import de.core.service.Services;
import de.shd.device.ExportData;

public interface UpdateService extends Service {
	
  public interface UpdateListener extends Serializable {
	  public void onUpdate(ExportData data) throws CoreException;
  }
	
  @Function default void update(@Param("data") ExportData data) throws CoreException {
    Services.invoke((new Call(getClass().getName(), "update")).addParameter("data", data));
  }
  
  @Function void register(UpdateListener listener) throws CoreException;
}
