package de.core.http.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.core.CoreException;
import de.core.cache.Cache;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.mime.MimeTypes;
import de.core.log.Logger;
import de.core.rt.Resource;
import de.core.serialize.annotation.Element;
import de.core.utils.FileUtils;

public abstract class AbstractFileRequestHandler extends AbstractHttpRequestHandler {
    protected class Range {
        long start;
        long end;
        long length;
        long total;

        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }
    }
    
    public static interface HttpResource {
    	public byte[] getData() throws CoreException;
    	public byte[] getData(Range range) throws CoreException;
    	public default String getContentType() {
    		return "application/octet-stream";
    	}
    	public long getLength() throws CoreException;
    	public String getName();
    }
	
    public static class FileRessource implements HttpResource {
    	protected Path path;
    	
		public FileRessource(Path path) {
			super();
			this.path = path;
		}

		@Override
		public byte[] getData() throws CoreException {
			try {
				return Files.readAllBytes(path);
			} catch(Throwable t) {
				CoreException.throwCoreException(t);
			}
			return null;
		}

		@Override
		public String getContentType() {
			return MimeTypes.getMimeType(FileUtils.getFileExtension(path), "application/octet-stream");
		}

		@Override
		public byte[] getData(Range range) throws CoreException {
			try (RandomAccessFile input=new RandomAccessFile(path.toFile(), "r")){
				return copy(input,range.start,range.length);
			} catch(Throwable t) {
				CoreException.throwCoreException(t);
			}
			return null;
		}

		@Override
		public long getLength() throws CoreException {
			try {
				return Files.size(path);
			} catch(Throwable t) {
				CoreException.throwCoreException(t);
			}
			return -1;
		}

		@Override
		public String getName() {
			return path.getFileName().toString();
		}
    }
    
    public static class MemoryRessource implements HttpResource {
    	protected byte[] data;
    	protected String name;
    	protected String contentType;
		public MemoryRessource(String name, String contentType, byte[] data) {
			super();
			this.name = name;
			this.contentType=contentType;
			this.data = data;
		}
		@Override
		public byte[] getData() throws CoreException {
			return data;
		}
		@Override
		public byte[] getData(Range range) throws CoreException {
			throw new CoreException("Not supported");
		}
		@Override
		public long getLength() throws CoreException {
			return data!=null?data.length:0;
		}
		@Override
		public String getName() {
			return name;
		}
		@Override
		public String getContentType() {
			return contentType;
		}
    }
    
	@Element protected String root;
	@Element(defaultValue = "index.html") protected String welcomFile;

	Logger log = Logger.createLogger("StaticFileRequestHandler");

	public AbstractFileRequestHandler(String contextPath, String root) {
		super(contextPath);
	}
	
	public AbstractFileRequestHandler() {
	}
	
	public abstract HttpResource getRequestedResource(HttpRequest request);

	public HttpResponse handleRequest(HttpRequest request) {
		String requestedResource = request.getRequestPath();
		if (requestedResource.equals(this.contextPath.startsWith("/") ? this.contextPath : ("/" + this.contextPath))) {
			FixedLengthHttpResponse response = new FixedLengthHttpResponse("FORWARDED".getBytes());
			response.addHeader(new HttpHeader("Location",requestedResource + (this.welcomFile.startsWith("/") ? this.welcomFile : ("/" + this.welcomFile))));
			response.setStatusCode(302);
			return response;
		}
		requestedResource = requestedResource.replace(this.contextPath, "");
		requestedResource = this.root + requestedResource;
		if (requestedResource.endsWith(this.contextPath))
			requestedResource = requestedResource + "/" + this.welcomFile;
		
		HttpResource resource=getRequestedResource(request);
		if(resource!=null) {
			try {
				if(request.getHeaderValue("Range")!=null) {
					try {
						return handleRangeRequest(request, resource);
					} catch (Throwable t) {
						return new FixedLengthHttpResponse("error".getBytes(), 500);
					}
				} else {
					DefaultResponse response = new DefaultResponse();
					response.setContentType(resource.getContentType());
					response.setIs(new ByteArrayInputStream(resource.getData()));
					return response;
				}
			} catch(Throwable t) {
				return new FixedLengthHttpResponse("error".getBytes(),500);
			}
		} else {
			return new FixedLengthHttpResponse("not found".getBytes(), 404);
		}
	}
	
	public HttpResponse handleRangeRequest(HttpRequest request, HttpResource resource) throws Exception {
		String range=request.getHeaderValue("Range");
		long length=resource.getLength();
		String disposition = "inline";
		String contentType=resource.getContentType();
		if (!contentType.startsWith("image")) {
            String accept = request.getHeaderValue("Accept");
            disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";
        }
		DefaultResponse response=new DefaultResponse();
		response.setContentType(contentType);
        response.addHeader("Content-Disposition", disposition + ";filename=\"" + resource.getName() + "\"");
        response.addHeader("Accept-Ranges", "bytes");
		
        if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
            response.addHeader("Content-Range", "bytes */" + length); // Required in 416.
            response.setStatusCode(416);
            return response;
        }
        List<Range> ranges=new ArrayList<Range>();
        Range full = new Range(0, length - 1, length);
        if (ranges.isEmpty()) {
            for (String part : range.substring(6).split(",")) {
                long start = sublong(part, 0, part.indexOf("-"));
                long end = sublong(part, part.indexOf("-") + 1, part.length());

                if (start == -1) {
                    start = length - end;
                    end = length - 1;
                } else if (end == -1 || end > length - 1) {
                    end = length - 1;
                }
                // Check if Range is syntactically valid. If not, then return 416.
                if (start > end) {
                    response.addHeader("Content-Range", "bytes */" + length); // Required in 416.
                    response.setStatusCode(416);
                    return response;
                }
                // Add range.
                ranges.add(new Range(start, end, length));
            }
        }
        if (ranges.isEmpty() || ranges.get(0) == full) {
            Range r = full;
            response.setContentType(contentType);
            response.addHeader("Content-Length", String.valueOf(r.length));
            response.setIs(new ByteArrayInputStream(resource.getData(r)));
            
        } else if (ranges.size() == 1) {
            // Return single part of file.
            Range r = ranges.get(0);
            response.setContentType(contentType);
            response.addHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
            response.addHeader("Content-Length", String.valueOf(r.length));
            response.setStatusCode(206);
            response.setIs(new ByteArrayInputStream(resource.getData(r)));
        } 
        return response;
	}
	
    private static boolean accepts(String acceptHeader, String toAccept) {
        String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
        Arrays.sort(acceptValues);
        return Arrays.binarySearch(acceptValues, toAccept) > -1
            || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
            || Arrays.binarySearch(acceptValues, "*/*") > -1;
    }
	
    private static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

	public boolean keepAlive() {
		return false;
	}

    private static byte[] copy(RandomAccessFile input,long start, long length) throws IOException {
    	byte[] buffer = new byte[16*1024];
        ByteArrayOutputStream output=new ByteArrayOutputStream();
    	int read;
        if (input.length() == length) {
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        } else {
            input.seek(start);
            long toRead = length;
            while ((read = input.read(buffer)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    break;
                }
            }
        }
        return output.toByteArray();
    }
}
