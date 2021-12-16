package de.core.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChunkedInputStream extends FilterInputStream {
  int chunkSize = 0;
  
  boolean first = true;
  
  protected ChunkedInputStream(InputStream in) {
    super(in);
  }
  
  public int read() throws IOException {
    if (this.chunkSize == 0)
      readChunkSize(); 
    this.chunkSize--;
    return this.in.read();
  }
  
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    if (this.chunkSize == 0) {
      readChunkSize();
      if (this.chunkSize == 0)
        return -1; 
    } 
    int toread = Math.min(this.chunkSize, len);
    int inread = this.in.read(b, off, toread);
    this.chunkSize -= inread;
    return inread;
  }
  
  private void readChunkSize() throws IOException {
    this.chunkSize = -1;
    boolean cr = false;
    StringBuilder sb = new StringBuilder();
    if (!this.first) {
      this.in.read();
      this.in.read();
    } 
    char b;
    while ((b = (char)this.in.read()) != -1) {
      if (b == '\r') {
        cr = true;
        continue;
      } 
      if (cr && b == '\n') {
        this.chunkSize = Integer.parseInt(sb.toString(), 16);
        this.first = false;
        return;
      } 
      sb.append(b);
    } 
  }
}
