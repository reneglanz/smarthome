package de.core.http.websocket;

public class WebSocketFrame implements Cloneable {
  protected boolean fin;
  
  protected Opcode opcode;
  
  protected boolean mask;
  
  protected long length;
  
  protected byte[] content;
  
  public enum Opcode {
    COTINUATION(0),
    TEXT(1),
    BINARY(2),
    CLOSE(8),
    PING(9),
    PONG(10);
    
    int intvalue;
    
    Opcode(int intvalue) {
      this.intvalue = intvalue;
    }
    
    public static Opcode get(int invalue) {
      for (Opcode opcode : values()) {
        if (opcode.intvalue == invalue)
          return opcode; 
      } 
      return null;
    }
  }
  
  public WebSocketFrame() {}
  
  public WebSocketFrame(Opcode opcode, byte[] payload) {
    this.opcode = opcode;
    this.content = payload;
    this.fin = true;
    this.mask = false;
    this.length = payload.length;
  }
  
  public static WebSocketFrame createPing() {
    return new WebSocketFrame(Opcode.PING, new byte[0]);
  }
  
  public boolean isMessageFrame() {
    return (this.opcode == Opcode.TEXT || this.opcode == Opcode.BINARY);
  }
  
  public byte[] getPayload() {
    return this.content;
  }
  
  public boolean isFin() {
    return this.fin;
  }
  
  public void setFin(boolean fin) {
    this.fin = fin;
  }
  
  public Opcode getOpcode() {
    return this.opcode;
  }
  
  public void setOpcode(Opcode opcode) {
    this.opcode = opcode;
  }
  
  public boolean isMask() {
    return this.mask;
  }
  
  public void setMask(boolean mask) {
    this.mask = mask;
  }
  
  public long getLength() {
    return this.length;
  }
  
  public void setLength(long length) {
    this.length = length;
  }
  
  public byte[] getContent() {
    return this.content;
  }
  
  public void setContent(byte[] content) {
    this.content = content;
  }
  
  public WebSocketFrame clone() throws CloneNotSupportedException {
    return (WebSocketFrame)super.clone();
  }
}
