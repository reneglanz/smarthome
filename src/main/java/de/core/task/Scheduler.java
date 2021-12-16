package de.core.task;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.rt.Releasable;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Scheduler extends Thread implements Releasable {
  protected Logger logger = Logger.createLogger("Scheduler");
  
  private enum Mode {
    DEFAULT, RECALCULATE;
  }
  
  public static class ExecutionPlanEntry implements Serializable {
    public static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    
    @Element
    String next;
    
    @Element
    String task;
    
    public ExecutionPlanEntry(String next, String task) {
      this.next = next;
      this.task = task;
    }
    
    public ExecutionPlanEntry() {}
  }
  
  private class ExecutionDetails implements Comparable<ExecutionDetails> {
    public long next = -1L;
    
    public long last = -1L;
    
    public Task task;
    
    public ExecutionDetails(Task task) {
      this.task = task;
      Scheduler.this.calculate(this);
    }
    
    public int compareTo(ExecutionDetails o) {
      return (new Long(this.next)).compareTo(Long.valueOf(o.next));
    }
    
    public String toString() {
      return "ExecutionDetails [next=" + new Date(this.next) + "]";
    }
  }
  
  private class TaskExecutor implements Runnable {
    Scheduler.ExecutionDetails details;
    
    private TaskExecutor(Scheduler.ExecutionDetails details) {
      this.details = details;
    }
    
    public void run() {
      try {
        Scheduler.this.logger.debug("Execute " + this.details.task.toString());
        this.details.task.execute();
        Scheduler.this.calculate(this.details);
        this.details.last = System.currentTimeMillis();
      } catch (CoreException e) {
        e.printStackTrace();
      } 
    }
  }
  
  @Element(defaultValue = "4")
  protected int corePoolSize = 5;
  
  @Element(defaultValue = "8")
  protected int maxPoolSize = 7;
  
  @Element(defaultValue = "100")
  protected int blockingList = 100;
  
  @Element(defaultValue = "ThreadManager")
  protected String name = "ThreadManager";
  
  protected ThreadPoolExecutor threadPool;
  
  protected List<ExecutionDetails> tasks = Collections.synchronizedList(new ArrayList<>());
  
  protected boolean run = true;
  
  protected Logger log = Logger.createLogger("Scheduler");
  
  protected Mode mode = Mode.DEFAULT;
  
  private Object SYNC = new Object();
  
  public Scheduler() {
    this.threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, 5L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
  }
  
  protected void calculate(ExecutionDetails details) {
    try {
      synchronized (this.SYNC) {
        details.next = details.task.next(-1L);
        this.mode = Mode.RECALCULATE;
        this.SYNC.notify();
      } 
    } catch (CoreException e) {
      details.next = Long.MAX_VALUE;
    } 
  }
  
  public void schedule(Task task) {
    this.tasks.add(new ExecutionDetails(task));
    this.logger.debug("Schedule task " + task.getId() + " " + task.toString());
    if (this.tasks.size() == 1)
      synchronized (this.SYNC) {
        this.SYNC.notify();
      }  
  }
  
  public long getWait(long now) {
    long min = 60000L;
    for (ExecutionDetails ed : this.tasks)
      min = Math.min(min, ed.next - now); 
    return min;
  }
  
  public void run() {
    while (this.run) {
      synchronized (this.SYNC) {
        try {
          if (this.tasks.size() > 0) {
            long now = System.currentTimeMillis();
            if (this.mode == Mode.DEFAULT)
              for (ExecutionDetails details : this.tasks) {
                if (details.next <= now) {
                  details.next = Long.MAX_VALUE;
                  this.threadPool.execute(new TaskExecutor(details));
                } 
              }  
            long wait = getWait(now);
            this.mode = Mode.DEFAULT;
            if (wait > 0L)
              this.SYNC.wait(wait); 
          } else {
            this.SYNC.wait();
          } 
        } catch (InterruptedException interruptedException) {}
      } 
    } 
  }
  
  public void release() throws CoreException {
    this.run = false;
    synchronized (this.SYNC) {
      this.SYNC.notify();
    } 
    this.threadPool.shutdown();
  }
  
  public void cancel(Task task) {
    synchronized (this.SYNC) {
      for (int i = this.tasks.size() - 1; i >= 0; i--) {
        if (((ExecutionDetails)this.tasks.get(i)).task.getId().equals(task.getId())) {
          this.tasks.remove(i);
          this.logger.debug("Removed task " + task.getId() + " " + task.toString());
        } 
      } 
    } 
  }
  
  public List<ExecutionPlanEntry> getExcutionPlan() {
    List<ExecutionPlanEntry> list = new ArrayList<>();
    this.tasks.forEach(details -> list.add(new ExecutionPlanEntry(ExecutionPlanEntry.format.format(new Date(details.next)), details.task.toString())));
    return list;
  }
}
