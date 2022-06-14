package de.shd.device;

import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	@Element String thumbnailUrl;
	
	protected Path videoFile;
	protected Path thumbnailFile;
	
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
		return videoFile;
	}

	public void setFile(Path file) {
		this.videoFile = file;
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
		if(this.videoFile!=null) try {
			this.id=URLEncoder.encode(Key.createKey(videoFile.toString()),"UTF-8");
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
	}

	public Path getThumbnailFile() {
		return thumbnailFile;
	}
	
	public void setThumbnailUrl(String thumbnail) {
		this.thumbnailUrl = thumbnail;
	}
	
	public void setThumbnailFile(Path thumbnailFile) {
		this.thumbnailFile = thumbnailFile;
	}

	@Override
	public int compareTo(Recording o) {
		return new Long(date).compareTo(new Long(o.date));
	}
}

