package de.core.client;

import de.core.CoreException;
import de.core.rt.Scope;
import de.core.serialize.Coding;
import de.core.service.Call;
import de.core.service.Services;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cli {
  public static final String PARAM_PREFIX_SHORT = "-";
  
  public static final String PARAM_PREFIX_LONG = "--";
  
  public static final String PARAM_HOST = "host";
  
  public static final String PARAM_HOST_SHORT = "h";
  
  public static final String PARAM_SERVICEID = "serviceid";
  
  public static final String PARAM_SERVICEID_SHORT = "s";
  
  public static final String PARAM_PROVIDERID = "providerid";
  
  public static final String PARAM_PROVIDERID_SHORT = "p";
  
  public static final String PARAM_METHOD = "method";
  
  public static final String PARAM_METHOD_SHORT = "m";
  
  public static final String PARAM_OUT_SHORT = "o";
  
  public static final String PARAM_OUT = "out";
  
  public void call(String[] args) throws CoreException, IOException {
    String serviceid = null, providerid = null, method = null, host = null, out = null;
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      String param = args[i];
      if (args.length <= i + 1)
        CoreException.throwCoreException("Missing value for parameter " + param); 
      if (args[i].startsWith("-")) {
        param = param.substring(1);
        if ("p".equals(param)) {
          providerid = args[i + 1];
        } else if ("s".equals(param)) {
          serviceid = args[i + 1];
        } else if ("m".equals(param)) {
          method = args[i + 1];
        } else if ("h".equals(param)) {
          host = args[i + 1];
        } else if ("o".equals(param)) {
          out = args[i + 1];
        } 
        i++;
      } else if (param.startsWith("--")) {
        param = param.substring(2);
        if ("providerid".equals(param)) {
          providerid = args[i + 1];
        } else if ("serviceid".equals(param)) {
          serviceid = args[i + 1];
        } else if ("method".equals(param)) {
          method = args[i + 1];
        } else if ("host".equals(param)) {
          host = args[i + 1];
        } else if ("out".equals(param)) {
          out = args[i + 1];
        } 
        i++;
      } else {
        params.put(param, args[i + 1]);
        i++;
      } 
    } 
    if (host != null) {
      Scope.setHost(host);
    } else {
      host = System.getProperty("services.host");
      if (host != null) {
        Scope.setHost(host);
      } else {
        Scope.setHost("localhost:7000");
      } 
    } 
    Call call = new Call(serviceid, method);
    if (providerid != null)
      call.setProvider(providerid); 
    call.setParameter(params);
    Object response = Services.invoke(call);
    byte[] output = null;
    if (response != null && response instanceof de.core.serialize.Serializable) {
      output = Coding.encode(response);
    } else if (response != null && List.class.isAssignableFrom(response.getClass())) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      List<?> respList = (List)response;
      for (Object o : respList) {
        if (o instanceof de.core.serialize.Serializable) {
          baos.write(Coding.encode(o));
        } else {
          baos.write(o.toString().getBytes());
        } 
        baos.write("\n".getBytes());
      } 
      output = baos.toByteArray();
    } else if (response != null) {
      try {
        output = response.toString().getBytes("UTF-8");
      } catch (UnsupportedEncodingException unsupportedEncodingException) {}
    } 
    if (output != null)
      if (out != null) {
        try {
          Files.write(Paths.get(out, new String[0]), output, new java.nio.file.OpenOption[0]);
        } catch (Throwable t) {
          CoreException.throwCoreException(t);
        } 
      } else {
        System.out.println(new String(output));
      }  
  }
  
  public static void main(String[] args) throws CoreException, IOException {
    (new Cli()).call(args);
  }
}
