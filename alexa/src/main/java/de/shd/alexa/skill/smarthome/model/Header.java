package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Header implements Serializable {
  @Element
  protected String namespace;
  
  @Element
  protected String name;
  
  @Element
  protected String payloadVersion;
  
  @Element
  protected String messageId;
  
  @Element
  protected String correlationToken;
  
  public String getNamespace() {
    return this.namespace;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getPayloadVersion() {
    return this.payloadVersion;
  }
  
  public String getMessageId() {
    return this.messageId;
  }
  
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void setPayloadVersion(String payloadVersion) {
    this.payloadVersion = payloadVersion;
  }
  
  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }
}
