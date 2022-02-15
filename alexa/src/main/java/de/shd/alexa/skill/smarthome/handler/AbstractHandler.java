package de.shd.alexa.skill.smarthome.handler;

import de.core.service.Services;
import de.shd.device.DeviceProvider;

public abstract class AbstractHandler implements Handler {
  DeviceProvider deviceStore;
  
  public DeviceProvider getDeviceStore() {
    if (this.deviceStore == null)
      this.deviceStore = (DeviceProvider)Services.getProvider("devices"); 
    return this.deviceStore;
  }
}
