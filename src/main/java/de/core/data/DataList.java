package de.core.data;

import de.core.serialize.annotation.Element;
import java.util.ArrayList;
import java.util.List;

public class DataList implements Data {
  @Element
  protected List<Data> listdata;
  
  public DataList() {
    this.listdata = new ArrayList<>();
  }
  
  public DataList(List<Data> data) {
    this.listdata = data;
  }
  
  public List<Data> getData() {
    return this.listdata;
  }
  
  public void setData(List<Data> data) {
    this.listdata = data;
  }
  
  public void add(Data data) {
    this.listdata.add(data);
  }
  
  public void add(List<Data> data) {
    this.listdata.addAll(data);
  }
}
