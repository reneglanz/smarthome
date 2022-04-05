package de.shd.device;

import java.net.URLEncoder;
import java.nio.file.Path;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.utils.Key;

public class Recording implements Serializable, Comparable<Recording> {
	@Element String id;
	@Element long size;
	@Element long date;
	@Element String url;
	@Element String contentType;
	@Element String thumbnail;
	protected Path file;
	
	public Recording() {}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String video) {
		this.url = video;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void createId() throws CoreException {
		if(this.file!=null) try {
			this.id=URLEncoder.encode(Key.createKey(file.toString()),"UTF-8");
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
	}

	@Override
	public int compareTo(Recording o) {
		return new Long(date).compareTo(new Long(o.date));
	}
}

