package de.esp8266.ota;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.log.Logger;
import de.core.serialize.annotation.Element;

public class Ota extends AbstractHttpRequestHandler {

	private static final Logger log=Logger.createLogger("ESP8266-Ota");
	@Element String dataDir;
	
	@Override
	public HttpResponse handleRequest(HttpRequest request) {
		String path=request.getRequestPath();
		String[] token=path.substring(1).split("/");
		
		if(token.length==3) {
			String image=token[1];
			Path dir=Paths.get(dataDir,image,token[2]);
			if(Files.exists(dir)) try {
						FixedLengthHttpResponse response=new FixedLengthHttpResponse(Files.readAllBytes(dir));
						response.addHeader(new HttpHeader("Content-Type", "application/octet-stream"));
						response.addHeader(new HttpHeader("Content-Disposition",
								"attachment; filename=\"" + dir.getFileName().toString() + "\""));
						return response;
			} catch(Throwable t) {
				return new FixedLengthHttpResponse(t.getMessage().getBytes(), 500);
			} else {
				return new FixedLengthHttpResponse("Not found".getBytes(), 404);
			}
		}
		return new FixedLengthHttpResponse("error".getBytes(), 500);
	}

	@Override
	public boolean keepAlive() {
		return false;
	}


}
