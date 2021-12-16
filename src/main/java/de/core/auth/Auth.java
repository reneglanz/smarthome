package de.core.auth;

import de.core.service.Service;

public interface Auth extends Service {
  Token login(String paramString1, String paramString2);
}
