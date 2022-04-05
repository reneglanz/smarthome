package de.core.http;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class ChunkedOutputStream extends FilterOutputStream {

	protected int chunkSize=-1;
	protected static final byte[] CRLF=new byte[] {13,10};
	
	public ChunkedOutputStream(OutputStream out, int chunkSize) {
		super(out);
		this.chunkSize=chunkSize;
	}

	@Override
	public void write(int b) throws IOException {
		throw new IOException("not supported");
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	@Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0)
            return;
        out.write(String.format("%x\r\n", len).getBytes());
        out.write(b, off, len);
        out.write("\r\n".getBytes());
    }

    public void finish() throws IOException {
        out.write("0\r\n\r\n".getBytes());
    }
	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ChunkedOutputStream cos=new ChunkedOutputStream(baos, 20);
		byte[] ba="Hallo Test Hallo Test Hallo Test Hallo Test Hallo Test Hallo Test Hallo Test".getBytes();
		cos.write(ba, 0, ba.length);
		System.out.println(new String(baos.toByteArray()));
		
	}
}
