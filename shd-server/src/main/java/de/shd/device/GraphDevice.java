package de.shd.device;

import de.core.CoreException;
import de.core.Env;
import de.core.data.Data;
import de.core.data.DataList;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.store.DBStore;
import de.core.task.CronTask;
import de.core.task.Scheduler;
import de.core.task.Task;

public class GraphDevice extends AbstractDevice implements Launchable, Releasable {
  @Element(mandatory = true)
  protected String storeName;
  
  @Element
  protected String cronPattern;
  
  @Element
  protected DBStore.Filter filter;
  
  @Injectable
  Scheduler scheduler;
  
  private class UpdateTask extends CronTask {
    public UpdateTask() throws CoreException {
      super(GraphDevice.this.cronPattern);
    }
    
    public void execute() {
      GraphDevice.this.updateData();
    }
  }
  
  protected DataList data = new DataList();
  
  protected CronTask task;
  
  protected DBStore store;
  
  public void launch() throws CoreException {
    if (this.cronPattern != null) {
      this.task = new UpdateTask();
      this.scheduler.schedule((Task)this.task);
    } 
  }
  
  public void updateData() {
    if (this.storeName != null && this.store == null)
      this.store = (DBStore)Env.get(this.storeName); 
    if (this.store != null)
      try {
        this.data.setData(this.store.get(this.filter));
        export();
      } catch (Throwable t) {
        t.printStackTrace();
      }  
  }
  
  public void release() throws CoreException {
    if (this.task != null)
      this.scheduler.cancel((Task)this.task); 
  }
  
  public ExportData createExportData() {
    return new ExportData(getDeviceHandle(), name, (Data)this.data);
  }
}
