package de.shd.automation.action;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.serialize.Coding;
import de.core.serialize.annotation.Element;
import de.shd.automation.Data;

public class DebugAction implements Action {
  @Element String loggerName;
  
  protected DebugAction() {}
  
  public DebugAction(String loggerName) {
    this.loggerName = loggerName;
  }
  
  public void execute(Data data) throws CoreException {
    Logger logger = Logger.createLogger(this.loggerName);
    logger.debug(new String(Coding.encode(data)));
  }
}
