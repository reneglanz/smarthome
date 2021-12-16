package de.shd.ui;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceFile implements Serializable, Iterable<ResourceFile.Entry> {
  @Element
  protected List<Entry> files;
  
  @Element
  protected int version;
  
  public static class Entry implements Serializable {
    @Element
    String path;
    
    @Element
    String hash;
    
    protected Entry() {}
    
    public Entry(String path, String hash) {
      this.path = path;
      this.hash = hash;
    }
    
    public boolean equals(Object obj) {
      if (this == obj)
        return true; 
      if (obj == null)
        return false; 
      if (getClass() != obj.getClass())
        return false; 
      Entry other = (Entry)obj;
      if (this.hash == null) {
        if (other.hash != null)
          return false; 
      } else if (!this.hash.equals(other.hash)) {
        return false;
      } 
      if (this.path == null) {
        if (other.path != null)
          return false; 
      } else if (!this.path.equals(other.path)) {
        return false;
      } 
      return true;
    }
  }
  
  public void add(String path, String hash) {
    if (this.files == null)
      this.files = new ArrayList<>(); 
    this.files.add(new Entry(path, hash));
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    ResourceFile other = (ResourceFile)obj;
    if (this.files == null) {
      if (other.files != null)
        return false; 
    } else if (!this.files.equals(other.files)) {
      return false;
    } 
    return true;
  }
  
  public void updateVersion(ResourceFile old) {
    old.version++;
  }

	@Override
	public Iterator<Entry> iterator() {
		return files.iterator();
	}
}
