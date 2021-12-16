package de.core.rt;

import de.core.serialize.annotation.Element;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class PooledThreads {
  protected ThreadPoolExecutor threadPool;
  
  @Element(defaultValue = "10")
  protected int corePoolSize = 10;
  
  @Element(defaultValue = "10")
  protected int maximumPoolSize = 10;
  
  @Element(defaultValue = "5")
  protected int keepAlive = 5;
  
  @Element(defaultValue = "100")
  protected int queueSize = 100;
  
  public PooledThreads() {
    this.threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize, this.keepAlive, TimeUnit.SECONDS, new ArrayBlockingQueue<>(this.queueSize));
  }
  
  public void execute(Runnable runnable) {
    this.threadPool.execute(runnable);
  }
}
