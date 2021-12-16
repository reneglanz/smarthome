package de.core.db;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

@Injectable
public class DBConnectionManager implements Serializable {
  @Element(mandatory = true)
  private String jdbc;
  
  @Element
  private String user;
  
  @Element
  private String password;
  
  static {
    try {
      ServiceLoader.<Driver>load(Driver.class, DBConnectionManager.class.getClassLoader()).forEach(driver -> {
          
          });
    } catch (Throwable throwable) {}
  }
  
  @Element(defaultValue = "10")
  private static int maxPoolSize = 10;
  
  private List<DBConnection> connectionPool = new Vector<>();
  
  private List<DBConnection> usedConnection = new Vector<>();
  
  private CountDownLatch connectionLatch;
  
  public void init(String jdbc, String user, String password) {
    this.jdbc = jdbc;
    this.user = user;
    this.password = password;
  }
  
  public synchronized DBConnection get() throws SQLException {
    if (this.connectionPool.isEmpty())
      if (this.usedConnection.size() < maxPoolSize) {
        this.connectionPool.add(create());
      } else {
        if (this.connectionLatch == null)
          this.connectionLatch = new CountDownLatch(1); 
        try {
          this.connectionLatch.await();
        } catch (InterruptedException interruptedException) {}
      }  
    DBConnection connection = this.connectionPool.remove(this.connectionPool.size() - 1);
    this.usedConnection.add(connection);
    return connection;
  }
  
  private DBConnection create() throws SQLException {
    return new DBConnection(DriverManager.getConnection(this.jdbc, this.user, this.password), this);
  }
  
  public synchronized void release(DBConnection connection) {
    this.usedConnection.remove(connection);
    this.connectionPool.add(connection);
    if (this.connectionLatch != null) {
      this.connectionLatch.countDown();
      this.connectionLatch = null;
    } 
  }
}
