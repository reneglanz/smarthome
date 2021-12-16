package de.shd.alexa.skill.smarthome;

import de.core.CoreException;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class Capability implements Serializable {
  @Element protected String type;
  @Element(name = "interface")protected String _interface;
  @Element protected String instance;
  @Element protected String version;
  @Element protected Properties properties;
  @Element protected CapabilityRessources capabilityResources;
  @Element protected RangeConfiguration configuration;
  @Element protected Sematics semantics;
  
  
  public static class Properties implements Serializable {
    @Element
    protected Capability.Property[] supported;
    
    @Element
    protected boolean retrievable = true;
  }
  
  public static class Property implements Serializable {
    @Element
    protected String name;
    
    protected Property() {}
    
    protected Property(String name) {
      this.name = name;
    }
  }
  
  public static class CapabilityRessources implements Serializable {
	  @Element protected FriendlyName[] friendlyNames;
  }
  
  public static class FriendlyName implements Serializable {
	  @Element(name="@type") protected String type="asset";
	  @Element protected FriendlyNameValue value;
	  public FriendlyName(String assetId) {
		  value=new FriendlyNameValue(assetId);
	  }
  }
  
  public static class FriendlyNameValue implements Serializable {
	  @Element protected String assetId;
	  public FriendlyNameValue(String assetId) {
		  this.assetId=assetId;
	  }
  }
  
  public static class RangeConfiguration implements Serializable {
	  @Element protected SupportedRange supportedRange=new SupportedRange();
	  @Element protected String unitOfMeasure;
  }
  
  public static class SupportedRange implements Serializable {
	  @Element protected int minimumValue=0;
	  @Element protected int maximumValue=100;
	  @Element protected int precision=1;
  }
  
  public static class ActionMapping implements Serializable {
	  @Element(name="@type") protected String type="ActionsToDirective";
	  @Element protected String[] actions;
	  @Element protected Directive directive;
  }
  
  public static class Directive implements Serializable {
	  @Element protected String name;
	  @Element protected RangePayload payload;
	  
	  public Directive(String name, int payload) {
		  this.name=name;
		  this.payload=new RangePayload(payload);
	  }
  }
  
  public static class RangePayload implements Serializable {
	  @Element protected int rangeValue;
	  public RangePayload(int rangeValue) {
		  this.rangeValue=rangeValue;
	  }
  }
  
  public static class Sematics implements Serializable {
	  @Element ActionMapping[] actionMappings;
	  @Element StateMapping[] stateMappings;
  }
  
  public static class StateMapping implements Serializable {
	  @Element(name="@type") protected String type="StatesToValue";
	  @Element protected String[] states;
	  @Element protected int value;
	  @Element protected Range range;
	  
	  public StateMapping(String state, int value) {
		  this.states=new String[] {state};
		  this.value=value;
	  }
	  
	  public StateMapping(String state, int min, int max) {
		  this.type="StatesToRange";
		  this.states=new String[] {state};
		  this.range=new Range();
		  this.range.minimumValue=min;
		  this.range.maximumValue=max;
	  }
  }
  
  public static class Range implements Serializable {
	  @Element protected int minimumValue=0;
	  @Element protected int maximumValue=100;
  }
  
  public static class Factory {
    public static Capability createPowerController() {
      Capability capability = new Capability();
      capability.type = "AlexaInterface";
      capability._interface = "Alexa.PowerController";
      capability.version = "3";
      capability.properties = new Capability.Properties();
      capability.properties.supported = new Capability.Property[] { new Capability.Property("powerState") };
      return capability;
    }
    
    public static Capability createAlexa() {
      Capability capability = new Capability();
      capability.type = "AlexaInterface";
      capability._interface = "Alexa";
      capability.version = "3";
      return capability;
    }
    
    public static Capability createBrightnessController() {
      Capability capability = new Capability();
      capability.type = "AlexaInterface";
      capability._interface = "Alexa.BrightnessController";
      capability.version = "3";
      capability.properties = new Capability.Properties();
      capability.properties.supported = new Capability.Property[] { new Capability.Property("brightness") };
      return capability;
    }
    
    public static Capability createColorController() {
      Capability capability = new Capability();
      capability.type = "AlexaInterface";
      capability._interface = "Alexa.ColorController";
      capability.version = "3";
      capability.properties = new Capability.Properties();
      capability.properties.supported = new Capability.Property[] { new Capability.Property("color") };
      return capability;
    }
    
    public static Capability createColorTempreture() {
      Capability capability = new Capability();
      capability.type = "AlexaInterface";
      capability._interface = "Alexa.ColorTemperatureController";
      capability.version = "3";
      capability.properties = new Capability.Properties();
      capability.properties.supported = new Capability.Property[] { new Capability.Property("colorTemperatureInKelvin") };
      return capability;
    }
    
    public static Capability createRollerRange() {
    	Capability capability=new Capability();
    	capability.type = "AlexaInterface";
    	capability._interface = "Alexa.RangeController";
    	capability.instance="Blind.Lift";
    	capability.version = "3";
    	capability.properties = new Capability.Properties();
    	capability.properties.supported=new Capability.Property[] { new Capability.Property("rangeValue") };
    	capability.capabilityResources=new CapabilityRessources();
    	capability.capabilityResources.friendlyNames=new FriendlyName[] {new FriendlyName("Alexa.Setting.Opening")};
    	capability.configuration=new RangeConfiguration();
    	capability.configuration.unitOfMeasure="Alexa.Unit.Percent";
    	capability.semantics=new Sematics();
    	capability.semantics.actionMappings=new ActionMapping[2];
    	
    	capability.semantics.actionMappings[0]=new ActionMapping();
    	capability.semantics.actionMappings[0].actions=new String[] {"Alexa.Actions.Close"};
    	capability.semantics.actionMappings[0].directive=new Directive("SetRangeValue",0);
    	
    	capability.semantics.actionMappings[1]=new ActionMapping();
    	capability.semantics.actionMappings[1].actions=new String[] {"Alexa.Actions.Open"};
    	capability.semantics.actionMappings[1].directive=new Directive("SetRangeValue",100);
    	
    	capability.semantics.stateMappings=new StateMapping[2];
    	capability.semantics.stateMappings[0]=new StateMapping("Alexa.States.Closed",0);
    	capability.semantics.stateMappings[1]=new StateMapping("Alexa.States.Open",1,100);
    	
    	return capability;
    }
    
    public static void main(String[] args) throws CoreException {
    	
		System.out.println(new String(Coding.encode(Capability.Factory.createRollerRange(), "json", true)));
	}
    
  }


}
