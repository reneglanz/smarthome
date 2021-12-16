package de.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection implements AutoCloseable {
  protected Connection connection;
  
  protected DBConnectionManager manager;
  
  public DBConnection(Connection connection, DBConnectionManager manager) {
    this.connection = connection;
    this.manager = manager;
  }
  
  public Statement createStatement() throws SQLException {
    return this.connection.createStatement();
  }
  
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return this.connection.prepareStatement(sql);
  }
  
  public void close() throws Exception {
    this.manager.release(this);
  }
}
