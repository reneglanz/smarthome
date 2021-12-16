package de.shd.alexa.skill.smarthome.requests;

import de.core.CoreException;
import de.core.serialize.Coding;
import de.shd.alexa.skill.smarthome.model.Request;

public class Test {
  public static void main(String[] args) throws CoreException {
    Request r = (Request)Coding.decode(Test.class.getResourceAsStream("discover.json"), "json", Request.class);
    r = r;
  }
}
