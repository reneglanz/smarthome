package de.core.service;

import de.core.CoreException;
import de.core.http.mime.MimeTypes;
import de.core.serialize.Coding;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RemoteServiceInvoker implements InvocationHandler, Invoker {

  protected String provider;
  protected String service;
  protected String host;
  protected int port;
  
  public RemoteServiceInvoker(String providerId, String serviceId, String host, int port) {
    this.provider = providerId;
    this.service = serviceId;
    this.host = host;
    this.port = port;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("getServiceHandle"))
      return this.service; 
    Function anno = method.<Function>getAnnotation(Function.class);
    String mname = (anno.value().length() > 0) ? anno.value() : method.getName();
    return invoke(new Call(this.provider, this.service, mname, argsToParameter(method, args)));
  }
  
  private Map<String, Object> argsToParameter(Method method, Object[] args) throws CoreException {
    HashMap<String, Object> params = new HashMap<>();
    Parameter[] parameter = method.getParameters();
    if (args == null && parameter.length == 0)
      return null; 
    if (parameter.length == args.length) {
      for (int i = 0; i < parameter.length; i++) {
        Param anno = parameter[i].<Param>getAnnotation(Param.class);
        if(anno!=null) {
        	params.put(anno.value(), args[i]);
        } else {
        	CoreException.throwCoreException("Missing Param annotation for " + method.getDeclaringClass().getName()+"."+method.getName());
        }
      } 
    } else {
      CoreException.throwCoreException("Arguments count does not match to Method");
    } 
    return params;
  }
  
  public <E> E invoke(Call call) throws CoreException {
    HttpURLConnection con = null;
    try {
      URL _url = new URL("http://" + this.host + ":" + this.port + "/services");
      con = (HttpURLConnection)_url.openConnection();
      con.setDoOutput(true);
      con.setDoInput(true);
      con.addRequestProperty("Content-Type", MimeTypes.getMimeType("sjos"));
      Coding.encode(call, con.getOutputStream(), "sjos");
      con.getOutputStream().close();
      con.connect();
      if (con.getContentLength() > 0) {
        Object obj = Coding.decode(con.getInputStream(), "sjos");
        if (obj instanceof ExceptionResponse) {
          CoreException.throwCoreException((ExceptionResponse)obj);
        } else if (obj instanceof CallResponse) {
          return (E)((CallResponse)obj).getValue();
        } 
        return (E)obj;
      } 
    } catch (Throwable t) {
      throw CoreException.throwCoreException(t);
    } finally {
      if (con != null)
        con.disconnect(); 
    } 
    return null;
  }
}
