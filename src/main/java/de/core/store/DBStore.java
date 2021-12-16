package de.core.store;

import de.core.CoreException;
import de.core.data.Data;
import de.core.rt.Resource;
import de.core.serialize.Serializable;
import java.util.List;

public interface DBStore extends Resource {
  void create() throws CoreException;
  
  void add(Data paramData) throws CoreException;
  
  List<Data> get(Filter paramFilter) throws CoreException;
  
  void update(Data paramData) throws CoreException;
  
  void delete(Filter paramFilter) throws CoreException;
  
  Data parseData(byte[] paramArrayOfbyte) throws CoreException;
  
  public static interface Filter extends Serializable {}
  
  public enum FilterCompare {
    EQUALS {
      public String toString() {
        return "=";
      }
    },
    LESSER {
      public String toString() {
        return "<";
      }
    },
    BIGGER {
      public String toString() {
        return ">";
      }
    },
    EQUALS_BIGGER {
      public String toString() {
        return ">=";
      }
    },
    EQUALS_LESSER {
      public String toString() {
        return "<=";
      }
    },
    NOT_EQUALS {
      public String toString() {
        return "!=";
      }
    };
    
    public static String toString(String columnName, FilterCompare compare) {
      return columnName + compare.toString() + "?";
    }
  }
  
  public static class LongKeyFilter implements Filter {
    protected long key;
    
    public LongKeyFilter(long key) {
      this.key = key;
    }
    
    public long getKey() {
      return this.key;
    }
    
    public void setKey(long key) {
      this.key = key;
    }
  }
}
