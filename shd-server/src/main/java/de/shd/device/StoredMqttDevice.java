package de.shd.device;

import de.core.CoreException;
import de.core.data.DataList;
import de.core.serialize.annotation.Element;
import de.core.service.Function;
import de.core.store.DBStore;

public class StoredMqttDevice extends MqttDevice {
  @Element protected DBStore.Filter filter;
  
  @Function
  public DataList getValues() throws CoreException {
	  return new DataList(this.store.get(this.filter));
  }
  
//  public ExportData createExportData() {
//    try {
//      return new ExportData(getDeviceHandle(), name, (de.core.data.Data)new Data((this.exportData != null) ? this.exportData.getValue() : "", this.store.get(this.filter)));
//    } catch (Exception e) {
//      return new ExportData(getDeviceHandle(), name, (de.core.data.Data)new Data((this.exportData != null) ? this.exportData.getValue() : "", null));
//    } 
//  }
}
