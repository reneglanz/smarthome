package de.core.task;

import java.util.UUID;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

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
  
  @Override
  public long next() throws CoreException {
    return this.patter.next();
  }
  
  public String getId() {
    return this.id;
  }
  
  public String toString() {
    return "CronTask [id=" + this.id + ", cronPattern=" + this.cronPattern + "]";
  }

}
