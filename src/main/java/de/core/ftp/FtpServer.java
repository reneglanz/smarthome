package de.core.ftp;

import de.core.CoreException;
import de.core.rt.Launchable;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.ListenerFactory;

public class FtpServer implements Serializable, Launchable {
  static class FtpFileHandler extends DefaultFtplet {
    List<IFtpFileHandler> handler = Collections.synchronizedList(new ArrayList<>());
    
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
      User user = session.getUser();
      FtpFile ftpFile = session.getFileSystemView().getWorkingDirectory();
      String ftpPath = ftpFile.getAbsolutePath() + "/" + request.getArgument();
      for (IFtpFileHandler h : this.handler) {
        if (h.canHandle(user.getName(), ftpPath))
          h.onFile(Paths.get(user.getHomeDirectory() + "/" + ftpPath, new String[0])); 
      } 
      return super.onUploadEnd(session, request);
    }
    
    public void register(IFtpFileHandler handler) {
      this.handler.add(handler);
    }
    
    public void deregister(IFtpFileHandler handler) {
    	this.handler.remove(handler);
    }
  }
  
  @Element(defaultValue = "21")
  protected int port = 21;
  
  @Element
  protected UserManager userManager;
  
  protected FtpServerFactory serverFactory;
  
  protected ListenerFactory listenerFactory;
  
  protected static FtpFileHandler ftlet = new FtpFileHandler();
  
  public void launch() throws CoreException {
    try {
      this.serverFactory = new FtpServerFactory();
      this.listenerFactory = new ListenerFactory();
      this.listenerFactory.setPort(this.port);
      this.serverFactory.addListener("default", this.listenerFactory.createListener());
      this.serverFactory.setUserManager(this.userManager);
      Map<String, Ftplet> ftplets = new HashMap<>();
      ftplets.put("default", ftlet);
      this.serverFactory.setFtplets(ftplets);
      this.serverFactory.createServer().start();
    } catch (Throwable t) {
      CoreException.throwCoreException(t);
    } 
  }
  
  public static void main(String[] args) throws CoreException {
    FtpServer s = new FtpServer();
    s.userManager = new UserManager();
    s.launch();
  }
}
