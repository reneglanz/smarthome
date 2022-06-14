package de.shd.automation.action;

import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.serialize.ClassAccessor;
import de.core.serialize.InlineSerializable;
import de.core.serialize.annotation.Element;
import de.core.serialize.elements.PrimitivElement;
import de.core.service.Call;
import de.core.service.Services;
import de.shd.automation.Data;

public class ServiceAction implements Action {
  @Element String provider;
  @Element String service;
  @Element String method;
  @Element protected Map<String, Object> parameter = new HashMap<>();
  @Element protected Map<String,String> parameterCast=new HashMap<>();
  
  public void execute(Data data) throws CoreException {
	Map<String,Object> callParameter=new HashMap<>();
	for(Map.Entry<String, Object> entry:parameter.entrySet()) {
		Object value;
		if(entry.getValue().toString().startsWith("$")) {
			value=data.get(entry.getValue().toString().substring(1), Object.class, null);
		} else {
			value=entry.getValue();
		}
		if(value!=null&&value instanceof String&&parameterCast.containsKey(entry.getKey())) {
			String strValue=(String)value;
			try {
				Class clazz=Class.forName(parameterCast.get(entry.getKey()));
				if(clazz.isEnum()) {
					value=Enum.valueOf(clazz, strValue);
				} else if(InlineSerializable.class.isAssignableFrom(clazz)) {
					InlineSerializable inline=(InlineSerializable) ClassAccessor.create(clazz).newInstance();
					inline.deserialize(new PrimitivElement(null, "xx", strValue));
					value=inline;
				}
				
			} catch(Throwable t) {
				
			}
			
		}
		callParameter.put(entry.getKey(), value);
	}
    Services.invoke(new Call(this.provider!=null?this.provider:Services.DEFAULT, this.service, this.method, callParameter));
  }
}
