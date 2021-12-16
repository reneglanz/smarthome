package de.core.http;

import de.core.io.LineInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestParser {
	public static HttpRequest parse(InputStream is) throws IOException {
		HttpRequest request = new HttpRequest();
		LineInputStream lis = new LineInputStream(is);
		String line = lis.readLine();
		String[] token = line.split(" ");
		if (token.length == 3) {
			request.method = token[0];
			request.setPath(token[1]);
			request.version = token[2];
		}
		request.header = paresHeader(lis);
		HttpHeader transferEncodiung = request.getHeader("Transfer-Encoding");
		if (transferEncodiung != null && "chunked".equals(transferEncodiung.value)) {
			request.is = new ChunkedInputStream(is);
		} else {
			request.is = is;
		}
		return request;
	}

	private static String parseFirstLine(InputStream is) throws IOException {
		StringBuilder buffer = new StringBuilder();
		int read = 0;
		boolean eol_cr = false;
		while ((read = is.read()) != -1 && read != 32) {
			if (13 == read) {
				eol_cr = true;
				continue;
			}
			if (10 == read && eol_cr)
				return buffer.toString();
			buffer.append((char) read);
		}
		return buffer.toString();
	}

	private static List<HttpHeader> paresHeader(LineInputStream is) throws IOException {
		String line = "";
		List<HttpHeader> list = new ArrayList<>();
		do {
			line = is.readLine();
			if (line == null)
				continue;
			int i = line.indexOf(":");
			//int p = line.indexOf(";");
			HttpHeader header = new HttpHeader();
			if (i <= 0)
				continue;
			header.setName(line.substring(0, i).trim());
//			if (!header.getName().equalsIgnoreCase("set-cookie")&&p != -1) {
//				header.setValue(line.substring(i + 1, p).trim());
//				header.setParameter(line.substring(p + 1, line.length()).trim());
//			} else {
				header.setValue(line.substring(i + 1, line.length()).trim());
//			}
			list.add(header);
		} while (line != null && line.length() > 0);
		return list;
	}
}
