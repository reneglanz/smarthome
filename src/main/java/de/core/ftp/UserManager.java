package de.core.ftp;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class UserManager implements org.apache.ftpserver.ftplet.UserManager, Serializable {
  public static class FtpUser implements Serializable {
    @Element
    protected String name;
    
    @Element
    protected String password;
    
    @Element
    protected String basedir;
    
    @Element(defaultValue = "true")
    protected boolean canWrite = true;
    
    @Element(defaultValue = "10")
    protected int maxLogins = 10;
    
    private BaseUser user;
    
    protected FtpUser() {}
    
    protected FtpUser(User user) {
      this.name = user.getName();
      this.password = user.getPassword();
      this.basedir = user.getHomeDirectory();
      for (Authority a : user.getAuthorities()) {
        if (a instanceof WritePermission)
          this.canWrite = true; 
      } 
    }
    
    public String getName() {
      return this.name;
    }
    
    public String getPassword() {
      return this.password;
    }
    
    public boolean isCanWrite() {
      return this.canWrite;
    }
    
    public int getMaxLogins() {
      return this.maxLogins;
    }
    
    public User toUser() {
      if (this.user == null)
        synchronized (FtpUser.class) {
          if (this.user == null) {
            this.user = new BaseUser();
            this.user.setName(this.name);
            this.user.setPassword(this.password);
            this.user.setHomeDirectory(this.basedir);
            List<Authority> authorities = new ArrayList<>();
            authorities.add(new ConcurrentLoginPermission(this.maxLogins, this.maxLogins));
            if (this.canWrite)
              authorities.add(new WritePermission()); 
            this.user.setAuthorities(authorities);
          } 
        }  
      return (User)this.user;
    }
    
    public void finish() {
      try {
        if (this.basedir != null && !Files.exists(Paths.get(this.basedir, new String[0]), new java.nio.file.LinkOption[0]))
          Files.createDirectories(Paths.get(this.basedir, new String[0]), (FileAttribute<?>[])new FileAttribute[0]); 
      } catch (Throwable throwable) {}
    }
  }
  
  @Element
  protected List<FtpUser> users = Collections.synchronizedList(new ArrayList<>());
  
  public void addUser(User user) throws FtpException {
    if (!doesExist(user.getName()))
      this.users.add(new FtpUser(user)); 
  }
  
  public User authenticate(Authentication auth) throws AuthenticationFailedException {
    if (auth instanceof UsernamePasswordAuthentication) {
      try {
        User user = getUserByName(((UsernamePasswordAuthentication)auth).getUsername());
        if (user != null && user.getPassword().equals(((UsernamePasswordAuthentication)auth).getPassword()))
          return user; 
      } catch (FtpException ftpException) {}
      throw new AuthenticationFailedException("Access denied");
    } 
    throw new AuthenticationFailedException("Access denied");
  }
  
  public void delete(String username) throws FtpException {}
  
  public boolean doesExist(String username) throws FtpException {
    return (getUserByName(username) != null);
  }
  
  public String getAdminName() throws FtpException {
    return null;
  }
  
  public String[] getAllUserNames() throws FtpException {
    return (String[])this.users.stream().map(u -> u.getName()).toArray(x$0 -> new String[x$0]);
  }
  
  public User getUserByName(String name) throws FtpException {
    for (FtpUser user : this.users) {
      if (user.getName().equals(name))
        return user.toUser(); 
    } 
    return null;
  }
  
  public boolean isAdmin(String arg0) throws FtpException {
    return false;
  }
  
  public void save(User user) throws FtpException {
    if (getUserByName(user.getName()) == null) {
      this.users.add(new FtpUser(user));
    } else {
      throw new FtpException("User with name " + user.getName() + " already exists");
    } 
  }
}
