package de.esp8266.ota;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.log.Logger;
import de.core.serialize.annotation.Element;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Esp8266OtaHandler extends AbstractHttpRequestHandler {
	protected Logger log = Logger.createLogger("Esp8266OtaHandler");

	@Element protected String dataDir;

	public HttpResponse handleRequest(HttpRequest request) {
		if (request.getRequestPath().endsWith("/version")) {
			Path dir = Paths.get(this.dataDir, new String[0]);
			try {
				Path firmware = Files.list(dir).filter(p -> p.getFileName().toString().endsWith(".bin")).findFirst()
						.get();
				String name = firmware.getFileName().toString();
				name = name.substring(0, name.indexOf("."));
				return (HttpResponse) new FixedLengthHttpResponse(name.getBytes());
			} catch (IOException iOException) {
			}
		} else if (request.getRequestPath().endsWith("/firmware")) {
			this.log.info("Send firmware to " + request.getThread().getSocket().getRemoteSocketAddress().toString());
			Path dir = Paths.get(this.dataDir, new String[0]);
			try {
				Path firmware = Files.list(dir).filter(p -> p.getFileName().toString().endsWith(".bin")).findFirst()
						.get();
				FixedLengthHttpResponse response = new FixedLengthHttpResponse(Files.readAllBytes(firmware));
				response.addHeader(new HttpHeader("Content-Type", "application/octet-stream"));
				response.addHeader(new HttpHeader("Content-Disposition",
						"attachment; filename=\"" + firmware.getFileName().toString() + "\""));
				return (HttpResponse) response;
			} catch (IOException iOException) {
			}
		}
		return null;
	}

	public boolean keepAlive() {
		return false;
	}
}
