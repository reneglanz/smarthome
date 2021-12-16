package de.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Exec implements Runnable {
	
	private String[] command;
	private ByteArrayOutputStream baosStd;
	private ByteArrayOutputStream baosErr;
	private int exitCode=-1;
	private Throwable err;
	private Process process;
	public Exec(String... command) {
		super();
		this.command = command;
	}

	public int sync() {
		run();
		return exitCode;
	}
	
	public void async() {
		new Thread(this).start();
	}

	public void run() {
		ProcessBuilder builder=new ProcessBuilder(command);
		try {
			process=builder.start();
			InputStream std=process.getInputStream();
			InputStream err=process.getErrorStream();
			
			byte[] ba=new byte[16*1024];
			int read=0;
			baosStd=new ByteArrayOutputStream();
			baosErr=new ByteArrayOutputStream();
			while((read=std.read(ba,0, Math.min(std.available(), ba.length)))!=-1&&process.isAlive()) {
				baosStd.write(ba, 0, read);
			}
			while((read=err.read(ba,0, Math.min(err.available(), ba.length)))!=-1&&process.isAlive()) {
				baosErr.write(ba, 0, read);
			}
			exitCode=process.exitValue();
		} catch (Throwable t) {
			err=t;
		}
	}
	
	public byte[] getStdOut() {
		return baosStd!=null?baosStd.toByteArray():new byte[0];
	}
	
	public byte[] getErrOut() {
		return baosErr!=null?baosErr.toByteArray():new byte[0];
	}
	
	public int getExitCode() {
		return exitCode;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	public Throwable getErr() {
		return err;
	}

	public void setErr(Throwable err) {
		this.err = err;
	}
	
	public boolean isAlive() {
		return process!=null?process.isAlive():false;
	}

}

