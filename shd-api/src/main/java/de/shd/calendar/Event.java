package de.shd.calendar;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Event implements Serializable {
  @Element
  String description;
  
  @Element
  long startDate;
  
  @Element
  long endDate;
  
  @Element
  String location;
  
  protected Event() {}
  
  public Event(long startDate, long endDate, String description, String location) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
    this.location = location;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public long getStartDate() {
    return this.startDate;
  }
  
  public void setStartDate(long startDate) {
    this.startDate = startDate;
  }
  
  public long getEndDate() {
    return this.endDate;
  }
  
  public void setEndDate(long endDate) {
    this.endDate = endDate;
  }
  
  public String getLocation() {
    return this.location;
  }
  
  public void setLocation(String location) {
    this.location = location;
  }
}
