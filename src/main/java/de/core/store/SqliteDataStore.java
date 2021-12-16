package de.core.store;

import de.core.db.DBConnectionManager;
import de.core.serialize.annotation.Element;

public abstract class SqliteDataStore implements DBStore {
  @Element
  protected String dbfile;
  
  protected DBConnectionManager manager;
  
  protected SqliteDataStore() {}
  
  public SqliteDataStore(String dbfile) {
    this.dbfile = dbfile;
    init();
  }
  
  public void finish() {
    init();
  }
  
  public void init() {
    this.manager = new DBConnectionManager();
    this.manager.init("jdbc:sqlite:" + this.dbfile, null, null);
  }
}
