package de.core.http.websocket;

import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.serialize.Coding;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WebSocket implements AutoCloseable {
  private static final String WEB_SOCKET_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  
  protected Socket socket;
  
  protected WebSocketState state;
  
  protected WebSocketManager manager;
  
  protected String path;
  
  public enum WebSocketState {
    CONNECTING, OPEN, CLOSED;
  }
  
  protected WebSocket(Socket socket, WebSocketManager manager) {
    this.socket = socket;
    this.manager = manager;
  }
  
  public HttpResponse handshake(HttpRequest request) {
    this.state = WebSocketState.CONNECTING;
    this.path = request.getRequestPath();
    HttpHeader secKey = request.getHeader("Sec-WebSocket-Key");
    if (secKey != null) {
      String websocketAccept = secKey.getValue() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] digestResult = digest.digest(websocketAccept.getBytes());
        FixedLengthHttpResponse response = new FixedLengthHttpResponse(new byte[0], 101);
        response.addHeader(new HttpHeader("Sec-WebSocket-Accept", Coding.toBase64(digestResult)));
        response.addHeader(new HttpHeader("Upgrade", "websocket"));
        response.addHeader(new HttpHeader("Connection", "Upgrade"));
        return (HttpResponse)response;
      } catch (NoSuchAlgorithmException noSuchAlgorithmException) {}
    } 
    return (HttpResponse)new FixedLengthHttpResponse("ERROR".getBytes(), 501);
  }
  
  public void loop() {
    this.state = WebSocketState.OPEN;
    while (this.state != WebSocketState.CLOSED) {
      try {
        WebSocketFrame frame = read(this.socket.getInputStream());
        if (frame.isMessageFrame()) {
          this.manager.fireOnMessageReceived(this.path, frame);
          continue;
        } 
        if (frame.getOpcode() == WebSocketFrame.Opcode.PING) {
          WebSocketFrame pong = frame.clone();
          pong.setOpcode(WebSocketFrame.Opcode.PONG);
          send(pong);
          continue;
        } 
        if (frame.getOpcode() == WebSocketFrame.Opcode.CLOSE)
          try {
            close();
          } catch (Exception exception) {} 
      } catch (WebSocketException e) {
        if (e.isCloseConnection())
          try {
            close();
          } catch (Exception exception) {} 
      } catch (SocketException e) {
        try {
          close();
        } catch (Exception exception) {}
      } catch (Throwable throwable) {}
    } 
  }
  
  public void setState(WebSocketState state) {
    this.state = state;
  }
  
  public WebSocketFrame read(InputStream istream) throws WebSocketException {
    WebSocketFrame frame = new WebSocketFrame();
    boolean timeoutToPing = false;
    try {
      timeoutToPing = true;
      int head = evalread(istream.read());
      timeoutToPing = false;
      frame.fin = ((head & 0x80) != 0);
      frame.opcode = WebSocketFrame.Opcode.get(head & 0xF);
      if (frame.opcode == null)
        throw new WebSocketException("Cloud not read valid opcode", true); 
      head = evalread(istream.read());
      frame.mask = ((head & 0x80) != 0);
      long length = (head & 0x7F);
      if (length == 126L) {
        byte[] ba = new byte[2];
        evalread(istream.read(ba));
        ByteBuffer buff = ByteBuffer.wrap(ba);
        length = (buff.getShort() & 0xFFFF);
      } else if (length == 127L) {
        byte[] ba = new byte[8];
        evalread(istream.read(ba));
        ByteBuffer buff = ByteBuffer.wrap(ba);
        length = buff.getLong() & 0xFFFFFFFFFFFFFFFFL;
      } 
      frame.length = length;
      byte[] maskkey = null;
      if (frame.mask) {
        maskkey = new byte[4];
        evalread(istream.read(maskkey));
      } 
      byte[] content = new byte[(int)length];
      int read = 0;
      while (read < length)
        read += evalread(istream.read(content, read, (int)length - read)); 
      if (frame.mask) {
        for (int i = 0; i < content.length; i++)
          content[i] = (byte)(content[i] ^ maskkey[i % 4]); 
        frame.content = content;
      } 
    } catch (SocketTimeoutException e) {
      if (timeoutToPing) {
        try {
          send(WebSocketFrame.createPing());
        } catch (IOException e1) {
          throw new WebSocketException(e, true);
        } 
      } else {
        throw new WebSocketException(e, true);
      } 
    } catch (Exception e) {
      if (e instanceof WebSocketException)
        throw (WebSocketException)e; 
    } 
    return frame;
  }
  
  private int evalread(int i) throws WebSocketException {
    if (i < 0)
      throw new WebSocketException("End of stream", true); 
    return i;
  }
  
  public void send(WebSocketFrame frame) throws IOException {
    OutputStream ostream = this.socket.getOutputStream();
    byte head = 0;
    if (frame.isFin())
      head = (byte)(head | 0x80); 
    head = (byte)(head | (frame.getOpcode()).intvalue & 0xF);
    ostream.write(head);
    int length = (int)frame.getLength();
    if (length <= 125) {
      if (frame.isMask()) {
        ostream.write(0x80 | (byte)length);
      } else {
        ostream.write((byte)length);
      } 
    } else if (length <= 65535) {
      ostream.write(frame.isMask() ? 254 : 126);
      ostream.write(length >>> 8);
      ostream.write(length);
    } 
    if (!frame.isMask())
      ostream.write(frame.getPayload()); 
  }
  
  public void close() throws Exception {
    this.state = WebSocketState.CLOSED;
    try {
      this.socket.close();
    } catch (IOException iOException) {}
    this.manager.removeWebSocket(this);
  }
}
