package de.core.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;

import de.core.CoreException;
import de.core.http.HttpHeader;
import de.core.http.HttpResponse;
import de.core.rt.Releasable;

public class DefaultResponse extends HttpResponse implements Releasable {

	private InputStream is;
	private ByteChannel chanel;
	private long length=-1;
	
	public DefaultResponse(InputStream is, long length) {
		this.is=is;
		this.length=length;
	}
	
	public DefaultResponse() {}
	
	public void setIs(InputStream is) {
		this.is = is;
	}

	public void setLength(long length) {
		this.length = length;
	}

	@Override
	public long getContentLength() {
		return length;
	}

	@Override
	public InputStream getContent() {
		return is;
	}

	@Override
	public void prepareForSend() {
		if(getHeader(HttpHeader.CONTENT_LENGTH)==null) {		
			addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, "" + getContentLength()));
		}
		addHeader(new HttpHeader("Server", "MyHTTP-Server"));
	}

	@Override
	public void release() throws CoreException {
		if(is!=null) {
			try {
				is.close();
			} catch (IOException e) {}
		}
	}

}
