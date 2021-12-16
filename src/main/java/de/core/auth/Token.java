package de.core.auth;

import de.core.CoreException;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.service.Services;

public class Token implements IToken, Serializable {
  @Element
  protected TokenData data;
  
  @Element
  protected byte[] signature;
  
  public static class TokenData implements Serializable {
    @Element
    String user;
    
    @Element
    long validTo;
    
    public byte[] getAsByteArray() {
      return (this.user + "" + this.validTo).getBytes();
    }
  }
  
  protected Token() {}
  
  public Token(TokenData data, byte[] signature) {
    this.data = data;
    this.signature = signature;
  }
  
  public String toString() {
    try {
      return Coding.toBase64(Coding.encode(this.data)) + "." + Coding.toBase64(this.signature);
    } catch (CoreException e) {
      return null;
    } 
  }
  
  public static Token parseToken(String tokenString) throws CoreException {
    String dataStr = tokenString.substring(0, tokenString.indexOf("."));
    String sig = tokenString.substring(tokenString.indexOf(".") + 1);
    TokenData data = (TokenData)Coding.decode(Coding.fromBase64(dataStr), "sjos");
    return new Token(data, Coding.fromBase64(sig));
  }
  
  public Token refresh() throws CoreException {
    return ((IToken)Services.get(IToken.class)).refresh();
  }
  
  public boolean validate() throws CoreException {
    return ((IToken)Services.get(IToken.class)).validate();
  }
}
