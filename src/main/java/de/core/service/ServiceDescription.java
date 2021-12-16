package de.core.service;

import java.security.Provider;

import de.core.serialize.annotation.Element;

public class ServiceDescription {

	public static class MethodDescription {
		public String name;
		public String[] params;
	}
	
	@Element protected String name;
	@Element protected String provider;
	
	public static ServiceDescription forService(Provider provider, Service service) {
		ServiceDescription desc=new ServiceDescription();
		desc.name=service.getServiceHandle().toString();
		desc.provider=provider.getName();
		return desc;		
	}
}
