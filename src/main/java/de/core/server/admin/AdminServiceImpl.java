package de.core.server.admin;

import de.core.CoreException;
import de.core.Env;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.log.Logger;
import de.core.server.Server;
import de.core.task.Scheduler;
import java.util.Collections;
import java.util.List;

public class AdminServiceImpl implements AdminService {
  protected Server server;
  
  public AdminServiceImpl(Server server) {
    this.server = server;
  }
  
  public void shutdown() throws CoreException {
    this.server.shutdown();
  }
  
  public List<Scheduler.ExecutionPlanEntry> schedules() throws CoreException {
    Scheduler scheduler = (Scheduler)Env.get(Scheduler.class);
    if (scheduler != null)
      return scheduler.getExcutionPlan(); 
    return Collections.emptyList();
  }
  
  public void setLogLevel(int level) throws CoreException {
    Logger.setRootLogLevel(level);
  }
  
  public NameHandle getServiceHandle() {
    return new NameHandle("admin");
  }
}
