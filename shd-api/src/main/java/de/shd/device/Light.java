package de.shd.device;

import de.core.CoreException;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.service.Function;
import de.core.service.Param;
import java.util.List;

public interface Light extends Switch {
  @Function
  void setBrightness(@Param("value") int paramInt) throws CoreException;
  
  @Function
  void setColorTemp(@Param("value") int paramInt) throws CoreException;
  
  @Function
  void setColor(@Param("color") Color paramColor) throws CoreException;
  
  @Function
  int getBrightness() throws CoreException;
  
  @Function
  int getColorTemp() throws CoreException;
  
  @Function
  boolean canDim() throws CoreException;
  
  @Function
  boolean canColorTemp() throws CoreException;
  
  @Function
  boolean canColor() throws CoreException;
  
  @Function
  List<Color> getColorPresets() throws CoreException;
  
  @Function
  Color getColor() throws CoreException;
  
  public static class Color implements Serializable {
    @Element
    int r;
    
    @Element
    int g;
    
    @Element
    int b;
    
    public Color(int r, int g, int b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }
    
    protected Color() {}
    
    public int getR() {
      return this.r;
    }
    
    public int getG() {
      return this.g;
    }
    
    public int getB() {
      return this.b;
    }
  }
}
