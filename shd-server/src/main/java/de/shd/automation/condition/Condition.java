package de.shd.automation.condition;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.automation.Data;

public class Condition implements Serializable {
  public enum ValueType {
	  DATE,
	  STRING,
	  NUMBER,
	  UNKNOWN
  }
		
  @Element protected Compare compare;
  @Element protected String value;
  @Element protected String key;
  
  @Element
  protected ValueType valueType=ValueType.UNKNOWN;
  
  public enum Compare {
    EQUALS, NOT_EQUALS, BIGGER, BIGGER_EQUALS, LESSER, LESSER_EQUALS;
  }
  
  public enum Operator {
    AND, OR;
  }
  
  @Element
  protected Operator operator = Operator.AND;
  
  protected Condition() {}
  
  public Condition(Operator operator, String key, Compare compare, String value) {
    this.compare = compare;
    this.value = value;
    this.operator = operator;
    this.key = key;
  }
  
  public Condition(String key, Compare compare, String value) {
    this(Operator.AND, key, compare, value);
  }
  
  public Condition(Compare compare, String value) {
    this(Operator.AND, "payload", compare, value);
  }
  
  public boolean resolve(Data data) throws CoreException {
    if (data != null) {
      String s = null;
      if (this.key != null && this.key.length() > 0) {
        s = (String)data.get(this.key, String.class, null);
        if (s == null)
          return false; 
      } else {
        s = (String)data.get("payload", String.class, "");
      } 
      Float f = null;
      Float vf = null;
      try {
        f = Float.valueOf(Float.parseFloat(s));
      } catch (NumberFormatException numberFormatException) {}
      try {
        vf = Float.valueOf(Float.parseFloat(this.value));
      } catch (NumberFormatException numberFormatException) {}
      if (this.compare != Compare.EQUALS && this.compare != Compare.NOT_EQUALS && f == null && vf == null)
        CoreException.throwCoreException("compare with BIGGER, BIGGER_EQUALS, LESSER or LESSER_EQUALS reqiures triggerData and value to be a number"); 
      switch (this.compare) {
        case EQUALS:
          return this.value.equals(s);
        case NOT_EQUALS:
          return !this.value.equals(s);
        case BIGGER:
          return (vf.floatValue() > f.floatValue());
        case BIGGER_EQUALS:
          return (vf.floatValue() >= f.floatValue());
        case LESSER:
          return (vf.floatValue() < f.floatValue());
        case LESSER_EQUALS:
          return (vf.floatValue() <= f.floatValue());
      } 
    } else {
      CoreException.throwCoreException("DefaultCondition requires trigger data");
    } 
    return false;
  }
  
  public Operator getOperator() {
    return this.operator;
  }
  
  public void setOperator(Operator operator) {
    this.operator = operator;
  }
}
