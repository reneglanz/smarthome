package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.Env;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.task.IntervalTask;
import de.core.task.Scheduler;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpTrigger extends AbstractTrigger implements Launchable, Releasable {
  @Element
  public String host;
  
  private class Task extends IntervalTask {
    public Task() {
      super(IpTrigger.this.checkInterval);
    }
    
    public void execute() throws CoreException {
      IpTrigger.this.check();
    }
  }
  
  @Element(defaultValue = "20000")
  public int checkInterval = 5000;
  
  @Element(defaultValue = "20000")
  public int checkTimeout = 20000;
  
  @Injectable
  Scheduler scheduler;
  
  protected boolean reachable = false;
  
  protected long changed = 0L;
  
  protected Task task;
  
  InetAddress inetAdress;
  
  protected IpTrigger() {}
  
  public IpTrigger(String host) {
    this.host = host;
  }
  
  public void launch() throws CoreException {
    if (this.scheduler == null)
      this.scheduler = (Scheduler)Env.get(Scheduler.class); 
    try {
      this.inetAdress = InetAddress.getByName(this.host);
    } catch (UnknownHostException unknownHostException) {}
    if (this.scheduler != null) {
      this.task = new Task();
      this.scheduler.schedule((de.core.task.Task)this.task);
    } 
  }
  
  public void check() {
    try {
      if (this.inetAdress != null) {
        boolean tmp = this.inetAdress.isReachable(this.checkTimeout);
        if (this.changed == -2L) {
          this.automation.getData().set("host", tmp ? "available" : "unavailable");
          this.automation.run();
          this.changed = -1L;
          this.reachable = tmp;
        } 
        if (tmp != this.reachable) {
          this.reachable = tmp;
          this.changed = System.currentTimeMillis();
        } 
      } 
      if (this.changed != -1L && System.currentTimeMillis() > this.changed) {
        this.changed = -1L;
        this.automation.getData().set("host", this.reachable ? "available" : "unavailable");
        this.automation.run();
      } 
    } catch (Throwable throwable) {}
  }
  
  public void release() throws CoreException {
    if (this.scheduler != null)
      this.scheduler.cancel((de.core.task.Task)this.task); 
  }
}
