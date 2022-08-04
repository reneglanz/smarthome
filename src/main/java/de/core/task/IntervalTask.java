package de.core.task;

import de.core.CoreException;
import de.core.serialize.annotation.Element;
import java.util.UUID;

public abstract class IntervalTask implements Task {
  @Element
  int interval;
  
  protected String id;
  
  public IntervalTask(int interval) {
    this.interval = interval;
    this.id = UUID.randomUUID().toString();
  }
  
  public long next() throws CoreException {
    return System.currentTimeMillis() + this.interval;
  }
  
  public String getId() {
    return this.id;
  }
  
  public String toString() {
    return "IntervalTask [id=" + this.id + ", interval=" + this.interval + "]";
  }
}
