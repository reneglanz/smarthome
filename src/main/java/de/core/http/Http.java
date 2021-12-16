package de.core.http;

import de.core.CoreException;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.utils.Streams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.activation.CommandInfo;


public class Http {

	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String CONTENT_LENGTH="Content-Length";

	public static class HttpRequest implements AutoCloseable {
		String url;
		HttpURLConnection con;
		String method;

		Map<String, String> header;

		public HttpRequest(String method, String url, Map<String, String> header) {
			this.method = method;
			this.url = url;
			this.header = header;
		}

		public HttpResponse send(InputStream istream) throws MalformedURLException, IOException {
			URL _url = new URL(this.url);
			this.con = (HttpURLConnection) _url.openConnection();
			this.con.setDoOutput(true);
			this.con.setDoInput(true);
			this.con.setRequestMethod(this.method);
			if (this.header != null) {
				this.header.entrySet().forEach(
						entry -> this.con.setRequestProperty((String) entry.getKey(), (String) entry.getValue()));
				
				if(!header.containsKey("user-agent")) {
					this.con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
				}
			}
			
			if (istream != null)
				Streams.copy(istream, this.con.getOutputStream());
			this.con.connect();
			
			FixedLengthHttpResponse response=new FixedLengthHttpResponse(
					con.getResponseCode()>=200&&con.getResponseCode()<=300
						?Streams.readAll(con.getInputStream())
						:Streams.readAll(con.getErrorStream()));
			for(Entry<String, List<String>> e:con.getHeaderFields().entrySet()) {
				e.getValue().stream().forEach((val)->{response.addHeader(new HttpHeader(e.getKey(),val));});
			}
			response.setStatusCode(con.getResponseCode());
			return response;
		}

		public void close() throws Exception {
			if (this.con != null)
				this.con.disconnect();
		}
	}

	public static byte[] get(String url) throws Exception {
		return Streams.readAll(get(url, new HashMap<>()).getContent());
	}

	public static HttpResponse get(String url, Map<String, String> header) throws Exception {
		try (HttpRequest httpGet = new HttpRequest("GET", url, header)) {
			return httpGet.send(null);
		}
	}

	public static HttpResponse post0(String url, Map<String, String> header, InputStream data) throws Exception {
		try (HttpRequest request = new HttpRequest("POST", url, header)) {
			return request.send(data);
		}
	}

	public static byte[] post(String url, Map<String, String> header, byte[] data) throws Exception {
		HttpResponse r=post0(url, header, (data == null) ? null : new ByteArrayInputStream(data));
		return Streams.readAll(r.getContent());
	}
}
