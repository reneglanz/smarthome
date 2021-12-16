package de.core.server.admin;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;
import de.core.task.Scheduler;
import java.util.List;

public interface AdminService extends Service {
  @Function
  void shutdown() throws CoreException;
  
  @Function
  List<Scheduler.ExecutionPlanEntry> schedules() throws CoreException;
  
  @Function
  void setLogLevel(@Param("level") int paramInt) throws CoreException;
}
