package de.core.ftp;

import java.nio.file.Path;

public interface IFtpFileHandler {
  boolean canHandle(String paramString1, String paramString2);
  
  void onFile(Path paramPath);
  
  default void registerFtpFileHandler() {
    FtpServer.ftlet.register(this);
  }
}
