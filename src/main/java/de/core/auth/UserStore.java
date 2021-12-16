package de.core.auth;

public interface UserStore {
  User getUser(String paramString1, String paramString2);
  
  void addUser(User paramUser);
  
  void delete(User paramUser);
  
  void updateUser(User paramUser);
}
