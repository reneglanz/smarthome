package de.core.rt;

import de.core.serialize.annotation.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadMananger extends Thread {
  @Element(defaultValue = "4")
  protected int corePoolSize = 10;
  
  @Element(defaultValue = "8")
  protected int maxPoolSize = 20;
  
  @Element(defaultValue = "100")
  protected int blockingList = 100;
  
  @Element(defaultValue = "ThreadManager")
  protected String name = "ThreadManager";
  
  protected ThreadPoolExecutor threadPool;
  
  protected List<ThreaedLaunchable> launchabled = new ArrayList<>();
  
  protected ThreadMananger() {
    this.threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, 5L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
  }
  
  public void run() {
    for (ThreaedLaunchable tl : this.launchabled) {
      if (tl.interval() > 0L);
    } 
  }
}
