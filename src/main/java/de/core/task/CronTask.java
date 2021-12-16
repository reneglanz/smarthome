package de.core.task;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.UUID;

public abstract class CronTask implements Task, Serializable {
  @Element
  protected String cronPattern;
  
  protected CronPattern patter;
  
  protected String id;
  
  protected CronTask() {}
  
  public CronTask(String cronPattern) throws CoreException {
    this.cronPattern = cronPattern;
    this.patter = CronPattern.get(cronPattern);
    this.id = UUID.randomUUID().toString();
  }
  
  public long next(long offset) throws CoreException {
    return this.patter.next(offset);
  }
  
  public String getId() {
    return this.id;
  }
  
  public String toString() {
    return "CronTask [id=" + this.id + ", cronPattern=" + this.cronPattern + "]";
  }
}
