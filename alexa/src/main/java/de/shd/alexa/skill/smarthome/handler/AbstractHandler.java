package de.shd.alexa.skill.smarthome.handler;

import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.service.Services;
import de.shd.device.DeviceProvider;

public abstract class AbstractHandler implements Handler {
  DeviceProvider deviceStore;
  
  public DeviceProvider getDeviceStore() {
    if (this.deviceStore == null)
      this.deviceStore = (DeviceProvider)Services.getProvider((Handle)new NameHandle("devices")); 
    return this.deviceStore;
  }
}
