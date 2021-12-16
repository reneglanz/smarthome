package de.core.client;

import de.core.serialize.annotation.Element;
import de.core.service.Service;

public class CliTest implements Service {
  @Element
  protected String name;
  
  public void sayName() {
    System.out.println(this.name);
  }
}
