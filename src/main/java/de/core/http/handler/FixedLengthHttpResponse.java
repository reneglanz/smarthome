package de.core.http.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.core.http.HttpHeader;
import de.core.http.HttpResponse;

public class FixedLengthHttpResponse extends HttpResponse {
	private byte[] content;

	public FixedLengthHttpResponse(byte[] content, int statusCode) {
		this.content = content;
		this.statusCode = statusCode;
		setContentType("text/plain");
	}

	public FixedLengthHttpResponse(byte[] content) {
		this(content, 200);
	}

	public long getContentLength() {
		return content != null ? this.content.length : 0;
	}

	public InputStream getContent() {
		return new ByteArrayInputStream(this.content);
	}

	public void prepareForSend() {
		removeHeader("Content-Length");
		removeHeader("Server");
		addHeader(new HttpHeader("Content-Length", "" + getContentLength()));
		addHeader(new HttpHeader("Server", "MyHTTP-Server"));
	}
	
	public void setContent(byte[] content) {
		this.content=content;
	}
}
