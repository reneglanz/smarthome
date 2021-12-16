package de.core.log;

public class ConsoleAppender implements Appender {
  public void write(String text) {
    System.out.println(text);
  }
}
