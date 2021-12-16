package de.shd.device;

import de.core.rt.Resource;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.util.ArrayList;
import java.util.List;

public class ColorPresets implements Serializable, Resource {
  @Element
  List<Light.Color> colors;
  
  public List<Light.Color> get() {
    return this.colors;
  }
  
  public ColorPresets() {
    this.colors = new ArrayList<>();
  }
  
  public void add(Light.Color color) {
    if (this.colors == null)
      this.colors = new ArrayList<>(); 
    this.colors.add(color);
  }
}
