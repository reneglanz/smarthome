package de.core.http;

import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
	protected String version;

	protected String method;

	protected String requestPath;

	protected List<HttpHeader> header;

	protected Map<String, String> requestParameter;

	protected InputStream is;

	protected HttpRequestThread thread;

	protected String connectorId;

	public void addHeader(HttpHeader _header) {
		if (this.header == null)
			this.header = new ArrayList<>();
		this.header.add(_header);
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequestPath() {
		return this.requestPath;
	}

	public void setPath(String path) {
		this.requestParameter = new HashMap<>();
		try {
			String tmp = path;
			if (tmp.contains("?")) {
				this.requestPath = tmp.substring(0, tmp.indexOf("?"));
				tmp = tmp.substring(tmp.indexOf("?") + 1, tmp.length()).trim();
				if (tmp.length() > 0) {
					StringBuffer buffer = new StringBuffer();
					String key = null;
					for (int i = 0; i < tmp.length(); i++) {
						char c = tmp.charAt(i);
						if (c == '=') {
							key = buffer.toString();
							buffer = new StringBuffer();
						} else if (c == '&') {
							this.requestParameter.put(URLDecoder.decode(key, "UTF-8"),
									URLDecoder.decode(buffer.toString(), "UTF-8"));
							key = null;
							buffer = new StringBuffer();
						} else {
							buffer.append(c);
						}
					}
					if (key != null && key.length() > 0)
						this.requestParameter.put(URLDecoder.decode(key, "UTF-8"),
								URLDecoder.decode(buffer.toString(), "UTF-8"));
				}
			} else {
				this.requestPath = path;
			}
		} catch (Exception exception) {
		}
	}

	public List<HttpHeader> getHeader() {
		return this.header;
	}

	public Map<String, String> getHeaderAsMap() {
		HashMap<String, String> headerMap = new HashMap<>();
		this.header.forEach(h -> {
			headerMap.put(h.getName(), h.getValue());
		});
		return headerMap;
	}

	public HttpHeader getHeader(String name) {
		for (HttpHeader header : this.header) {
			if (header.name.equals(name))
				return header;
		}
		return null;
	}

	public String getRequestParameter(String parameter) {
		return this.requestParameter.get(parameter);
	}

	public void setHeader(List<HttpHeader> header) {
		this.header = header;
	}

	public InputStream getIs() {
		return this.is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	public HttpRequestThread getThread() {
		return this.thread;
	}

	public String getConnectorId() {
		return this.connectorId;
	}

	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}
	
	public int getContentLength() {
		HttpHeader header=getHeader(Http.CONTENT_LENGTH);
		return header!=null?Integer.parseInt(header.getValue()):0;	}
}
