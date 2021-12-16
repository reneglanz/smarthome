package de.core.auth;

import de.core.CoreException;
import de.core.service.Service;

public interface IToken extends Service {
  Token refresh() throws CoreException;
  
  boolean validate() throws CoreException;
}
