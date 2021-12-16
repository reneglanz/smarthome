package de.core.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Logger {

  public static final int INFO = -1;
  public static final int ERROR = 0;
  public static final int WARN = 1;
  public static final int DEBUG = 2;
  public static int rootLevel = 0;
  
  public static HashMap<String, Logger> logger = new HashMap<>();
  public static ArrayList<Appender> appender = new ArrayList<>();
  public int logLevel = 0;
  public String name;
  
  public static Logger createLogger(String name) {
    Logger l = logger.get(name);
    if (l == null) {
      l = new Logger();
      l.name = name;
      l.logLevel = rootLevel;
      logger.put(name, l);
    } 
    return l;
  }
  
  public void debug(String msg) {
    log(msg, 2);
  }
  
  public void error(String msg) {
    log(msg, 0);
  }
  
  public void error(Throwable t) {
    log(t, 0);
  }
  
  public void info(Throwable t) {
    log(t, -1);
  }
  
  public void info(String msg) {
    log(msg, -1);
  }
  
  public void error(String msg, Throwable t) {
    error(msg);
    error(t);
  }
  
  public void log(Throwable t, int level) {
    if (this.logLevel >= level) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      if (this.logLevel >= level)
        appender.forEach(a -> a.write(levelToString(level) + " [" + this.name + "] [" + new Date() + "] " + sw.toString())); 
    } 
  }
  
  private void log(String msg, int level) {
    if (this.logLevel >= level)
      appender.forEach(a -> a.write(levelToString(level) + " [" + this.name + "] [" + new Date() + "] " + msg)); 
  }
  
  private String levelToString(int logLevel) {
    switch (logLevel) {
      case -1:
        return "[INFO]";
      case 0:
        return "[ERROR]";
      case 1:
        return "[WARN]";
      case 2:
        return "[DEBUG]";
    } 
    return "";
  }
  
  public static void setRootLogLevel(int level) {
    logger.entrySet().forEach(e -> ((Logger)e.getValue()).logLevel = level);
    rootLevel = level;
  }
}
