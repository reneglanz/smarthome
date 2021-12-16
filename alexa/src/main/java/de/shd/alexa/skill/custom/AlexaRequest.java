package de.shd.alexa.skill.custom;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.Map;

public class AlexaRequest implements Serializable {
  @Element
  protected String type;
  
  @Element
  protected String requestId;
  
  @Element
  protected String locale;
  
  @Element
  protected String timestamp;
  
  @Element
  protected Intent intent;
  
  public static class Intent implements Serializable {
    public static final String LAUNCH_REQUEST = "LaunchRequest";
    
    public static final String INTENT_REQUEST = "IntentRequest";
    
    @Element
    protected String name;
    
    @Element
    protected String confirmationStatus;
    
    @Element(genericValueType = AlexaRequest.Slot.class)
    protected Map<String, AlexaRequest.Slot> slots;
    
    public String getName() {
      return this.name;
    }
    
    public Map<String, AlexaRequest.Slot> getSlots() {
      return this.slots;
    }
  }
  
  public static class Slot implements Serializable {
    @Element
    protected String name;
    
    @Element
    protected String value;
    
    @Element
    protected String confirmationStatus;
  }
  
  public String getSlot(String name, String defaultValue) {
    if (this.intent != null && this.intent.slots != null) {
      Slot slot = this.intent.slots.get(name);
      if (slot != null)
        return (slot.value != null) ? slot.value : defaultValue; 
    } 
    return defaultValue;
  }
  
  public long getSlotAsLong(String name, long defaultValue) {
    String value = getSlot(name, null);
    if (value != null)
      return Long.parseLong(value); 
    return defaultValue;
  }
  
  public int getSlotAsInt(String name, int defaultValue) {
    String value = getSlot(name, null);
    if (value != null)
      return Integer.parseInt(value); 
    return defaultValue;
  }
  
  public String getIntentType() {
    return this.type;
  }
  
  public String getIntentName() {
    if (this.intent != null)
      return this.intent.getName(); 
    return null;
  }
}
