package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.Env;
import de.core.rt.Launchable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.task.CronTask;
import de.core.task.Scheduler;
import de.core.utils.SunriseSunset;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SunsetSunriseTrigger extends AbstractTrigger implements Launchable {
  @Injectable
  Scheduler scheduler;
  
  @Element
  double latitude;
  
  @Element
  double longitude;
  
  @Element
  Type type;
  
  protected Task task;
  
  private class Task extends CronTask {
    public Task(String cronPattern) throws CoreException {
      super(cronPattern);
    }
    
    public void execute() {
      try {
        SunsetSunriseTrigger.this.automation.getData().set(SunsetSunriseTrigger.this.dataKey(), SunsetSunriseTrigger.this.data(Long.valueOf(System.currentTimeMillis())));
        SunsetSunriseTrigger.this.runAutomation();
        SunsetSunriseTrigger.this.scheduler.cancel((de.core.task.Task)SunsetSunriseTrigger.this.task);
        Calendar tomorrow = new GregorianCalendar();
        tomorrow.add(5, 1);
        Calendar[] cal = SunriseSunset.getSunriseSunset(tomorrow, SunsetSunriseTrigger.this.latitude, SunsetSunriseTrigger.this.longitude);
        if (cal != null && cal.length == 2) {
          SunsetSunriseTrigger.this.task = SunsetSunriseTrigger.this.createTask(cal[SunsetSunriseTrigger.this.type.getIndex()]);
          SunsetSunriseTrigger.this.scheduler.schedule((de.core.task.Task)SunsetSunriseTrigger.this.task);
        } 
      } catch (CoreException e) {
        e.printStackTrace();
      } 
    }
  }
  
  public enum Type {
    SUNRISE(0),
    SUNSET(1);
    
    int index;
    
    Type(int index) {
      this.index = index;
    }
    
    int getIndex() {
      return this.index;
    }
  }
  
  protected Task createTask(Calendar cal) throws CoreException {
    return new Task("" + cal.get(12) + " " + cal.get(11) + " * * *");
  }
  
  public void launch() throws CoreException {
    Calendar[] cal = SunriseSunset.getSunriseSunset(new GregorianCalendar(), this.latitude, this.longitude);
    if (this.scheduler == null)
      this.scheduler = (Scheduler)Env.get(Scheduler.class); 
    if (cal != null && cal.length == 2) {
      this.task = createTask(cal[this.type.getIndex()]);
      this.scheduler.schedule((de.core.task.Task)this.task);
    } 
  }
}
