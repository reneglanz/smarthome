package de.shd.device;

import de.core.CoreException;
import de.core.data.Data;
import de.core.rt.Launchable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.task.CronTask;
import de.core.task.Scheduler;
import de.core.task.Task;
import de.core.utils.SunriseSunset;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SunTimeDevice extends AbstractDevice implements Sensor, Launchable {
  @Element double latitude;
  @Element double longitude;
  @Injectable Scheduler scheduler;
  
  private static class SunTimeData implements Data {
    @Element protected long sunrise;
    @Element protected long sunset;
    
    protected SunTimeData() {}
    
    protected SunTimeData(long sunrise, long sunset) {
      this.sunrise = sunrise;
      this.sunset = sunset;
    }
  }
  
  public ExportData createExportData() {
    Calendar[] times = SunriseSunset.getSunriseSunset(new GregorianCalendar(), this.latitude, this.longitude);
    return new ExportData(getDeviceId(), name, new SunTimeData(times[0].getTimeInMillis(), times[1].getTimeInMillis()));
  }
  
  public void launch() throws CoreException {
    if (this.scheduler != null)
      this.scheduler.schedule((Task)new CronTask("* * * * *") {
            public void execute() throws CoreException {
              SunTimeDevice.this.export();
            }
          }); 
  }
}
