package de.shd.automation.trigger;

import de.core.CoreException;
import de.core.data.Data;
import de.core.rt.Launchable;
import de.core.serialize.annotation.Element;
import de.core.service.Service;
import de.core.service.Services;
import de.shd.device.AbstractDevice;
import de.shd.device.ExportData;
import de.shd.device.Switch;
import de.shd.device.Toggle;
import de.shd.device.data.TextData;

public class ManualTrigger extends AbstractTrigger implements Launchable {
  @Element protected String provider = null;
  @Element protected boolean discoverable;
  
  public class ManualTriggerDevice extends AbstractDevice implements Toggle {
    protected ManualTriggerDevice setId(String automationId) {
      this.id = automationId;
      this.discoverable = ManualTrigger.this.discoverable;
      return this;
    }
    
    public ExportData createExportData() {
      return new ExportData(getDeviceId(), name, (Data)new TextData(""));
    }
    
    public Switch.State toggle() {
      try {
        ManualTrigger.this.automation.getData().set(ManualTrigger.this.dataKey(), ManualTrigger.this.data(null));
        ManualTrigger.this.runAutomation();
        return Switch.State.ON;
      } catch (CoreException e) {
        return Switch.State.UNKNOWN;
      } 
    }
  }
  
  public void trigger() throws CoreException {
    runAutomation();
  }
  
  public void launch() throws CoreException {
    ManualTriggerDevice trigger = (new ManualTriggerDevice()).setId(this.automation.getId());
    Services.bind(this.provider, (Service)trigger);
  }
}
