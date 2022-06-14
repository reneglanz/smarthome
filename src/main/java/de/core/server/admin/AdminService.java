package de.core.server.admin;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;
import de.core.service.ServiceDescription;
import de.core.task.Scheduler;
import java.util.List;

public interface AdminService extends Service {
  @Function
  void shutdown() throws CoreException;
  
  @Function
  List<Scheduler.ExecutionPlanEntry> schedules() throws CoreException;
  
  @Function
  void setLogLevel(@Param("level") int paramInt) throws CoreException;
  
  @Function
  public List<String> provider() throws CoreException;
  
  @Function
  public List<String> services(@Param("provider") String provider) throws CoreException;
  
  @Function
  public ServiceDescription describeService(@Param("providerId") String provider,@Param("serviceId") String service) throws CoreException;
  
  @Function
  public String getEndpointUrl(@Param("connector")String connector) throws CoreException;
}
