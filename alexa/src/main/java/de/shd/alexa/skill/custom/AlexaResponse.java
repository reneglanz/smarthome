package de.shd.alexa.skill.custom;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Element;
import de.core.serialize.elements.PrimitivElement;
import de.core.serialize.elements.Root;
import de.core.serialize.writer.CodingWriter;
import de.core.serialize.writer.DefaultWriter;
import de.core.serialize.writer.JsonWriter;
import java.io.ByteArrayOutputStream;

public class AlexaResponse implements Serializable {
  private String text;
  
  private String ssml;
  
  private boolean endSession = false;
  
  public AlexaResponse speak(String text) {
    this.text = text;
    return this;
  }
  
  public AlexaResponse endSession() {
    this.endSession = true;
    return this;
  }
  
  public byte[] getBytes() throws CoreException {
    Root root = new Root();
    root.add((Element)new PrimitivElement((Element)root, "version", "1.0"));
    ComplexElement response = new ComplexElement((Element)root, "response");
    root.add((Element)response);
    if (this.text != null) {
      ComplexElement outputSpeech = new ComplexElement((Element)response, "outputSpeech");
      response.add((Element)outputSpeech);
      outputSpeech.add((Element)new PrimitivElement((Element)outputSpeech, "type", "PlainText"));
      outputSpeech.add((Element)new PrimitivElement((Element)outputSpeech, "text", this.text));
    } 
    response.add((Element)new PrimitivElement((Element)response, "shouldEndSession", "" + this.endSession));
    root.add((Element)new PrimitivElement((Element)root, "shouldEndSession", Boolean.valueOf(false)));
    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    (new DefaultWriter()).write((ComplexElement)root, boas, (CodingWriter)new JsonWriter());
    return boas.toByteArray();
  }
  
  public static void main(String[] args) throws CoreException {
    System.out.println(new String((new AlexaResponse()).speak("test").getBytes()));
  }
}
