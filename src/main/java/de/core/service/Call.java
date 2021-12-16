package de.core.service;

import de.core.CoreException;
import de.core.auth.Token;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Call implements Serializable {
	@Element(inline = true)
	private Token token;

	@Element(inline = true, inlineClasses = { NameHandle.class })
	protected Handle service;
	@Element(inline = true, inlineClasses = { NameHandle.class })
	protected Handle provider;

	@Element
	protected String method;

	@Element
	protected String host;

	@Element
	protected int port;

	@Element
	protected Map<String, Object> parameter = new HashMap<>();

	protected Call() {
	}

	public Call(String host, int port, Token token, Handle provider, Handle service, String method,
			Map<String, Object> parameter) {
		this.host = host;
		this.port = port;
		this.token = token;
		this.service = service;
		this.provider = provider;
		this.method = method;
		this.parameter = parameter;
	}

	public Call(Handle providerId, Handle serviceId, String method) {
		this(null, -1, null, providerId, serviceId, method, new HashMap<>());
	}

	public Call(Handle serviceId, String method) {
		this(null, -1, null, null, serviceId, method, new HashMap<>());
	}

	public Call(Handle provider, Handle serviceId, String method, Map<String, Object> parameter) {
		this(null, -1, null, provider, serviceId, method, parameter);
	}

	public Call addParameter(String name, Object obj) {
		if (this.parameter == null)
			this.parameter = new HashMap<>();
		this.parameter.put(name, obj);
		return this;
	}

	public String getMethod() {
		return this.method;
	}

	public Map<String, Object> getParameter() {
		return this.parameter;
	}

	public Object getParameter(String key) {
		return this.parameter.get(key);
	}

	public Set<String> getParameterNames() {
		return this.parameter.keySet();
	}

	public Token getToken() {
		return this.token;
	}

	public boolean hasProviderId() {
		return (this.provider != null);
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Handle getService() {
		return this.service;
	}

	public Handle getProvider() {
		return this.provider;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public void setService(Handle service) {
		this.service = service;
	}

	public void setProvider(Handle provider) {
		this.provider = provider;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
	}
	
	public static void main(String[] args) throws CoreException {
		String str="{\r\n"
				+ "	@type=de.core.service.Call\r\n"
				+ "	service=tischlampe\r\n"
				+ "	provider=devices\r\n"
				+ "	method=toggle\r\n"
				+ "}\r\n"
				+ "";

		Call c1=Coding.decode(str);
		Call c=new Call(new NameHandle("devices"), new NameHandle("tischlampe"), "toggle");
		System.out.println(new String(Coding.encode(c)));
		
	}
}
