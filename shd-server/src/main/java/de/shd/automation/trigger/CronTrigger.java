package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.Env;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.task.CronTask;
import de.core.task.Scheduler;

public class CronTrigger extends AbstractTrigger implements Launchable, Releasable {
  @Element
  String cronPattern;
  
  @Injectable
  Scheduler executor;
  
  protected Task task;
  
  private class Task extends CronTask {
    public Task(String cronPattern) throws CoreException {
      super(cronPattern);
    }
    
    public void execute() {
      try {
        CronTrigger.this.runAutomation();
      } catch (CoreException e) {
        e.printStackTrace();
      } 
    }
  }
  
  protected CronTrigger() {}
  
  public CronTrigger(String pattern) {
    this.cronPattern = pattern;
  }
  
  public void launch() throws CoreException {
    if (this.executor == null)
      this.executor = (Scheduler)Env.get(Scheduler.class); 
    if (this.executor != null) {
      this.task = new Task(this.cronPattern);
      this.executor.schedule((de.core.task.Task)this.task);
    } 
  }
  
  public void release() throws CoreException {
    if (this.executor != null && this.task != null)
      this.executor.cancel((de.core.task.Task)this.task); 
  }

}
