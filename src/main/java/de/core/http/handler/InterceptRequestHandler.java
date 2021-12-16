package de.core.http.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import de.core.http.Http;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.serialize.annotation.Element;
import de.core.utils.Streams;

public class InterceptRequestHandler extends AbstractHttpRequestHandler {
	
	@Element protected String forward;
	HashMap<String, String> cookies=new HashMap<String, String>();
	
	@Override
	public HttpResponse handleRequest(HttpRequest request) {
		Http.HttpRequest target=new Http.HttpRequest(request.getMethod(),
													 (request.getRequestPath().startsWith("/target/")
														?"https://"+request.getRequestPath().replace("/target/","")
														:forward+request.getRequestPath()
													 ) ,
													 request.getHeaderAsMap());
		try {
			byte[] data=null;
			if("POST".equals(request.getMethod())&& request.getContentLength()>0){
				data=Streams.read(request.getIs(),request.getContentLength());
			}
			HttpResponse resp=target.send(data!=null?new ByteArrayInputStream(data):null);
			resp.removeHeader("Transfer-Encoding");
			resp.getHeader().forEach(h->{
				if("set-cookie".equalsIgnoreCase(h.getName())) {
					String[] cookies=h.getValue().split(";");
					String domainReplace="";
					for(int i=0;i<cookies.length;i++) {
						cookies[i]=cookies[i].trim();
						if(i==0&&cookies[i].indexOf("=")>-1) {
							String name=cookies[i].substring(0, cookies[i].indexOf("=")).trim();
							String value=cookies[i].substring(cookies[i].indexOf("=")+1,cookies[i].length()).trim();
							if(value.length()>0){
								this.cookies.put(name, value);
							}
						} else if(cookies[i].startsWith("Domain")||cookies[i].startsWith("domain")){
							domainReplace=cookies[i];
							cookies[i]="Domain=localhost";
						}
					}
					h.setValue(h.getValue().replace(domainReplace, "Domain=localhost"));
				}
			});
			if("/".equals(request.getRequestPath())) {
				adjustForm(resp);
			} else if(request.getRequestPath().contains("/ap/signin")) {
//				StringBuilder sb=new StringBuilder();
//				cookies.entrySet().stream().forEach(e->{sb.append(e.getKey()).append("=").append(e.getValue()).append("\n");});
//				return new FixedLengthHttpResponse(sb.toString().getBytes());
				return resp;
			}
			return resp;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new FixedLengthHttpResponse("ok".getBytes());
	}
	
	public void adjustForm(HttpResponse response) throws IOException {
		String data=new String(Streams.readAll(response.getContent()));
		data=data.replace("<form name=\"signIn\" method=\"post\" novalidate action=\"https://www.amazon.com/ap/signin",
				"<form name=\"signIn\" method=\"post\" novalidate action=\"http://localhost:7890/target/www.amazon.com/ap/signin");
		((FixedLengthHttpResponse)response).setContent(data.getBytes());
	}

	@Override
	public boolean keepAlive() {
		return false;
	}

}
