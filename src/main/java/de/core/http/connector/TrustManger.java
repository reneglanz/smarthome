package de.core.http.connector;

import java.io.FileInputStream;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

public class TrustManger extends X509ExtendedTrustManager {
  protected String dir;
  
  protected CertificateFactory factory;
  
  protected List<X509Certificate> trusted;
  
  public TrustManger() throws CertificateException {
    init();
  }
  
  protected void init() throws CertificateException {
    this.factory = CertificateFactory.getInstance("X.509");
    try (DirectoryStream<Path> dstream = Files.newDirectoryStream(Paths.get("D:\\dev\\workspaces\\dashboard\\deploy\\data\\root", new String[0]))) {
      this.trusted = new ArrayList<>();
      for (Path p : dstream) {
        try (FileInputStream fis = new FileInputStream(p.toFile())) {
          this.trusted.add((X509Certificate)this.factory.generateCertificate(fis));
        } catch (Throwable t) {
          t.printStackTrace();
        } 
      } 
    } catch (Throwable t) {
      t.printStackTrace();
    } 
  }
  
  public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    arg0 = arg0;
  }
  
  public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    arg0 = arg0;
  }
  
  public X509Certificate[] getAcceptedIssuers() {
    return this.trusted.<X509Certificate>toArray(new X509Certificate[this.trusted.size()]);
  }
  
  public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
    chain = chain;
  }
  
  public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
    chain = chain;
  }
  
  public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
    chain = chain;
  }
  
  public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
    chain = chain;
  }
}
