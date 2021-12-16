package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.alexa.skill.smarthome.Capability;

public class Endpoint implements Serializable {
  @Element
  Scope scope;
  
  @Element
  String endpointId;
  
  @Element
  String manufacturerName = "rg";
  
  @Element
  String friendlyName;
  
  @Element
  String description;
  
  @Element
  String[] displayCategories;
  
  @Element
  Cookies cookie = new Cookies();
  
  @Element
  Capability[] capabilities;
  
  public String getEndpointId() {
    return this.endpointId;
  }
  
  public String getManufacturerName() {
    return this.manufacturerName;
  }
  
  public String getFriendlyName() {
    return this.friendlyName;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public String[] getDisplayCategories() {
    return this.displayCategories;
  }
  
  public Capability[] getCapabilities() {
    return this.capabilities;
  }
  
  public void setEndpointId(String endpointId) {
    this.endpointId = endpointId;
  }
  
  public void setManufacturerName(String manufacturerName) {
    this.manufacturerName = manufacturerName;
  }
  
  public void setFriendlyName(String friendlyName) {
    this.friendlyName = friendlyName;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public void setDisplayCategories(String[] displayCategories) {
    this.displayCategories = displayCategories;
  }
  
  public void setCapabilities(Capability[] capabilities) {
    this.capabilities = capabilities;
  }
  
  public Scope getScope() {
    return this.scope;
  }
  
  public void setScope(Scope scope) {
    this.scope = scope;
  }
}
