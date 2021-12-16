package de.shd.alexa.skill.smarthome.model;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.shd.device.Light;
import java.awt.Color;

public class ColorHSB implements Serializable {
  @Element
  protected float hue;
  
  @Element
  protected float saturation;
  
  @Element
  protected float brightness;
  
  public ColorHSB() {}
  
  public ColorHSB(float hue, float saturation, float brightness) {
    this.hue = hue;
    this.saturation = saturation;
    this.brightness = brightness;
  }
  
  public double getHue() {
    return this.hue;
  }
  
  public double getSaturation() {
    return this.saturation;
  }
  
  public double getBrightness() {
    return this.brightness;
  }
  
  public Light.Color toColor() {
    Color awtcolor = Color.getHSBColor(this.hue, this.saturation, this.brightness);
    return new Light.Color(awtcolor.getRed(), awtcolor.getGreen(), awtcolor.getBlue());
  }
  
  public static ColorHSB forColor(Light.Color color) {
    float[] hsb = new float[3];
    Color.RGBtoHSB(color.getR(), color.getG(), color.getB(), hsb);
    ColorHSB colorhsb = new ColorHSB();
    colorhsb.hue = hsb[0];
    colorhsb.saturation = hsb[1];
    colorhsb.brightness = hsb[2];
    return colorhsb;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + Float.floatToIntBits(this.brightness);
    result = 31 * result + Float.floatToIntBits(this.hue);
    result = 31 * result + Float.floatToIntBits(this.saturation);
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    ColorHSB other = (ColorHSB)obj;
    if (Float.floatToIntBits(this.brightness) != Float.floatToIntBits(other.brightness))
      return false; 
    if (Float.floatToIntBits(this.hue) != Float.floatToIntBits(other.hue))
      return false; 
    if (Float.floatToIntBits(this.saturation) != Float.floatToIntBits(other.saturation))
      return false; 
    return true;
  }
}
