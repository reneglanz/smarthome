package de.shd.store;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.core.CoreException;
import de.core.data.Data;
import de.core.db.DBConnection;
import de.core.serialize.annotation.Element;
import de.core.store.DBStore;
import de.core.store.SqliteDataStore;

public class MqttDataStore extends SqliteDataStore {
  @Element protected String tablename;
  @Element(defaultValue="50") protected int limit=50;
  
  public static class MqttData implements Data {
    @Element public long date;
    @Element public float value;
  }
  
  public static class DataFilter implements DBStore.Filter {
    private long date;
    
    private DBStore.FilterCompare dateCompare;
    
    public long getDate() {
      return this.date;
    }
    
    public DBStore.FilterCompare getDateCompare() {
      return this.dateCompare;
    }
  }
  
  public void create() {
    if (!Files.exists(Paths.get(this.dbfile, new String[0]), new java.nio.file.LinkOption[0]))
      try (DBConnection con = this.manager.get()) {
        PreparedStatement stmt = con.prepareStatement("CREATE TABLE " + this.tablename + " (key INT, value REAL)");
        stmt.executeUpdate();
      } catch (Exception e) {
        e.printStackTrace();
      }  
  }
  
  public void add(Data data) throws CoreException {
    if (!(data instanceof MqttData))
      CoreException.throwCoreException("Not supported data type of " + data.getClass().toString()); 
    MqttData mqttData = (MqttData)data;
    try(DBConnection con = this.manager.get(); 
        PreparedStatement stmt = con.prepareStatement("INSERT INTO " + this.tablename + "(key, value) VALUES (?,?)")) {
      stmt.setLong(1, mqttData.date);
      stmt.setDouble(2, mqttData.value);
      stmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public List<Data> get(DBStore.Filter filter) {
    if (filter instanceof DataFilter) {
      DataFilter dataFilter = (DataFilter)filter;
      try(DBConnection con = this.manager.get(); 
          PreparedStatement stmt = con.prepareStatement("SELECT * FROM " + this.tablename + " WHERE key" + dataFilter.getDateCompare() + "?")) {
        stmt.setLong(1, dataFilter.getDate());
        stmt.setMaxRows(limit);
        stmt.execute();
        ResultSet rs = stmt.getResultSet();
        List<Data> list = new ArrayList<>();
        while (rs.next()) {
          MqttData data = new MqttData();
          data.date = rs.getLong("key");
          data.value = rs.getFloat("value");
          list.add(data);
        } 
        return list;
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return null;
  }
  
  public void update(Data data) throws CoreException {
    CoreException.throwCoreException("Update date is not suported in " + getClass().toString());
  }
  
  public void delete(DBStore.Filter filter) {
    if (filter instanceof DataFilter) {
      DataFilter pf = (DataFilter)filter;
      try(DBConnection con = this.manager.get(); 
          PreparedStatement stmt = con.prepareStatement("DELETE FROM " + this.tablename + " where " + DBStore.FilterCompare.toString("key", pf.dateCompare))) {
        stmt.setLong(1, pf.date);
        stmt.executeUpdate();
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
  }
  
  public Data parseData(byte[] bytes) throws CoreException {
    MqttData data = new MqttData();
    data.date = System.currentTimeMillis();
    data.value = Float.parseFloat(new String(bytes));
    return data;
  }
  
  public Object getName() {
    return "store.mqtt." + this.tablename;
  }
}
