package de.core.http.connector;

import de.core.CoreException;
import de.core.serialize.annotation.Element;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

public class SSLConnector extends DefaultConnector {
  @Element
  protected String key;
  
  @Element
  protected String certificate;
  
  @Element
  protected String acceptedCerts;
  
  @Element(defaultValue = "false")
  protected boolean clientAuth = false;
  
  private ServerSocketFactory socketFactory = null;
  
  public ServerSocketFactory getSocketFactory() throws Exception {
    if (this.socketFactory == null) {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null);
      keyStore.setKeyEntry("private", getPrivateKey(), "".toCharArray(), getChain());
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
      keyManagerFactory.init(keyStore, "".toCharArray());
      BouncyCastleJsseProvider provider = new BouncyCastleJsseProvider();
      Security.addProvider((Provider)provider);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      TrustManagerFactory trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManager.init((KeyStore)null);
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManager
          .getTrustManagers(), new SecureRandom());
      this.socketFactory = sslContext.getServerSocketFactory();
    } 
    return this.socketFactory;
  }
  
  public Certificate[] getChain() throws FileNotFoundException, IOException, CertificateException {
    List<Certificate> chain = new ArrayList<>();
    CertificateFactory fact = CertificateFactory.getInstance("X.509");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.certificate))))) {
      StringBuffer buffer = new StringBuffer();
      String line = "";
      while ((line = reader.readLine()) != null) {
        if ("-----END CERTIFICATE-----".equals(line)) {
          buffer.append(line + "\n");
          chain.add(fact.generateCertificate(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8"))));
          buffer = new StringBuffer();
          continue;
        } 
        buffer.append(line + "\n");
      } 
    } 
    return chain.<Certificate>toArray(new Certificate[chain.size()]);
  }
  
  public PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] content = Files.readAllBytes(Paths.get(this.key, new String[0]));
    String content0 = new String(content, "UTF-8");
    content0 = content0.replace("-----BEGIN PRIVATE KEY-----\n", "").replace("-----END PRIVATE KEY-----", "").replace("\n", "");
    Base64.Decoder b64 = Base64.getDecoder();
    byte[] key = b64.decode(content0);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
    return privateKey;
  }
  
  public ServerSocket createSocket() throws CoreException {
    try {
      SSLServerSocket socket = (SSLServerSocket)getSocketFactory().createServerSocket(this.port);
      socket.setNeedClientAuth(this.clientAuth);
      return socket;
    } catch (Throwable t) {
      CoreException.throwCoreException(t);
      return null;
    } 
  }
  
  public String getName() {
    return "SSL Connector [" + this.port + "]";
  }
}
