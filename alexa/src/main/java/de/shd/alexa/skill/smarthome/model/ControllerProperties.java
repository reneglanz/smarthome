package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.annotation.Element;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControllerProperties implements Properties {
  @Element
  protected String namespace;
  
  @Element
  protected String name;
  
  @Element
  protected String value;
  
  @Element
  protected String timeOfSample;
  
  @Element
  protected int uncertaintyInMilliseconds = 500;
  
  public ControllerProperties(String namespace, String name, String value) {
    this.namespace = namespace;
    this.name = name;
    this.value = value;
    SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss.SS");
    Date now = new Date();
    this.timeOfSample = dfDate.format(now) + "T" + dfTime.format(now) + "Z";
  }
}
