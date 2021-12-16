package de.core.http.handler;

import de.core.http.HttpRequest;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler, Serializable {
	@Element(defaultValue = "/") protected String contextPath = "/";
	@Element protected String forConnector = "any";

	protected AbstractHttpRequestHandler() {
	}

	public AbstractHttpRequestHandler(String contextPath) {
		this.contextPath = contextPath;
	}

	public boolean canHandleRequest(HttpRequest request) {
		if ("any".equals(this.forConnector)
				&& ("/".equals(contextPath) || request.getRequestPath().startsWith(this.contextPath)))
			return true;
		if (!"any".equals(this.forConnector)
			 && this.forConnector.equals(request.getConnectorId())
			&& ("/".equals(contextPath) || request.getRequestPath().startsWith(this.contextPath)) )
			return true;
		return false;
	}
}
