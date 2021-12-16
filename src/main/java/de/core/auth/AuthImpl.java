package de.core.auth;

import de.core.CoreException;
import de.core.serialize.Coding;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

public class AuthImpl implements Auth {
  private KeyPair key;
  
  protected UserStore userStore;
  
  public Token login(String username, String password) {
    if (this.key == null)
      createKeyPair(); 
    User user = this.userStore.getUser(username, password);
    if (user != null) {
      Token.TokenData data = new Token.TokenData();
      data.user = username;
      data.validTo = System.currentTimeMillis() + 1800000L;
      return new Token(data, sign(data));
    } 
    return null;
  }
  
  private void createKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(1024);
      this.key = keyPairGenerator.generateKeyPair();
    } catch (Throwable t) {
      t.printStackTrace();
    } 
  }
  
  private byte[] sign(Token.TokenData data) {
    try {
      Signature sig = Signature.getInstance("SHA256WithRSA");
      sig.initSign(this.key.getPrivate());
      sig.update(data.getAsByteArray());
      return sig.sign();
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    } 
  }
  
  public boolean validate(Token token) {
    try {
      Signature sig = Signature.getInstance("SHA256WithRSA");
      sig.initVerify(this.key.getPublic());
      sig.update(token.data.getAsByteArray());
      return sig.verify(token.signature);
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    } 
  }
  
  public static void main(String[] args) throws CoreException {
    AuthImpl impl = new AuthImpl();
    Token t = impl.login("root", "test");
    byte[] bat = Coding.encode(t);
    System.out.println(new String(bat));
    Token t2 = (Token)Coding.decode(bat);
    System.out.println(t.toString());
    System.out.println(t2.toString());
  }
}
