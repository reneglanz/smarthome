package de.shd.store;

import de.core.store.DBStore;

public class LastDayFilter extends MqttDataStore.DataFilter {
  public long getDate() {
    return System.currentTimeMillis() - 86400000L;
  }
  
  public DBStore.FilterCompare getDateCompare() {
    return DBStore.FilterCompare.EQUALS_BIGGER;
  }
}
