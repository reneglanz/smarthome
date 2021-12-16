package de.core.service;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.log.Logger;
import de.core.rt.Scope;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Services {
  private static final NameHandle DEFAULT = new NameHandle("default");
  
  private static HashMap<Handle, ServiceProvider<?>> provider = new HashMap<>();
  
  static {
    addProvider(new LocalServiceProvider(DEFAULT));
  }
  
  private static Logger logger = Logger.createLogger("Services");
  
  private static Invoker localInvoker = new Invoker() {
		public <E> E invoke(Call call) throws CoreException {
			ServiceProvider<Service> provider0 = null;
			if(call.hasProviderId()) {
				provider0 = (ServiceProvider) Services.provider.get(call.getProvider());
			} else {
				provider0 = (ServiceProvider) Services.provider.get(Services.DEFAULT);
			}
			if(provider0 != null) {
				Service service = provider0.getService(call.getService());
				if(service != null) {
					Method m = Services.findMethod(service.getClass(), call.getMethod(), call.getParameterNames());
					if(m != null) try {
						return (E) m.invoke(service, Services.toArgsArray(m, call.getParameter()));
					} catch (Exception e) {
						CoreException.throwCoreException("Failed to invoke method " + m.getName());
					} else {
						CoreException.throwCoreException("No such method for " + service.getClass().getName()
								+ ((service.getServiceHandle() != null) ? ("[" + service.getServiceHandle() + "]")
										: ""));
					}
				} else {
					CoreException.throwCoreException("Service " + call.getService() + " not bound "
							+ (call.hasProviderId() ? ("on provider " + call.getProvider()) : ""));
				}
			} else {
				CoreException.throwCoreException("Provider " + call.getProvider() + " not found");
			}
			return null;
		}
   };
  
  public static void bind(Service service) throws CoreException {
    bind((Handle)DEFAULT, service);
  }
  
  public static void bind(Handle providerId, Service service) throws CoreException {
    ServiceProvider<Service> provider0 = (ServiceProvider)provider.get((providerId != null) ? providerId : DEFAULT);
    if(provider0 != null) {
    	provider0.bind(service);
    	logger.info("Bound service " + service.getClass().toString() + ((service.getServiceHandle() != null) ? ("[" + service.getServiceHandle() + "]") : "") + " provider " + provider0.getProviderId());
    } 
  }
  
  public static void unbind(Service service) throws CoreException {
    unbind(null, service);
  }
  
  public static void unbind(String providerId, Service service) throws CoreException {
    ServiceProvider<Service> provider0 = (ServiceProvider)provider.get((providerId != null) ? providerId : DEFAULT);
    if(provider0 != null) {
    	provider0.unbind(service);
    	logger.info("Unbound service " + service.getClass().toString() + ((service.getServiceHandle() != null) ? ("[" + service.getServiceHandle() + "]") : "") + " from provider " + provider0);
    } else {
    	CoreException.throwCoreException("No such provider " + providerId);
    } 
  }
  
  public static <E> E get(Class<E> serviceClazz) throws CoreException {
	return get(null, null, serviceClazz);
  }	
  
	public static <E> E get(Handle providerId, Handle serviceId, Class<E> type) throws CoreException {
		ServiceProvider<Service> provider0 = (ServiceProvider) provider.get((providerId != null) ? providerId : DEFAULT);
		Service service = null;
		if (provider0 != null) {
			if (serviceId != null && type == null) {
				service = provider0.getService(serviceId);
			} else if (serviceId == null && type != null) {
				service = provider0.getService((Class) type);
			} else if (serviceId != null && type != null) {
				service = provider0.getService(serviceId);
				if (service != null && !service.implements0(type))
					CoreException.throwCoreException("Serive does not implement expected type");
			} else if (serviceId == null && type == null) {
				CoreException.throwCoreException("serviceId and/or type must be given");
			}
		}
		if (service != null)
			return (E) service;
		if (Scope.getScope() == Scope.REMOTE)
			return (E) createRemotes(providerId, serviceId, (Class) type);
		return null;
	}
  
  public static void addProvider(ServiceProvider<?> provider) {
    Services.provider.put(provider.getProviderId(), provider);
  }
  
  public static ServiceProvider<?> getProvider(Handle handle) {
    return provider.get(handle);
  }
  
  public static ServiceProvider<?> getProvider(Class<?> clazz) {
    for (Map.Entry<Handle, ServiceProvider<?>> entry : provider.entrySet()) {
      if (clazz.isAssignableFrom(((ServiceProvider)entry.getValue()).getClass()))
        return entry.getValue(); 
    } 
    return null;
  }
  
  public static ServiceProvider<?> getProvider(Service service) throws CoreException{
	  for (Map.Entry<Handle, ServiceProvider<?>> entry : provider.entrySet()) {
		  Service service1=entry.getValue().getService(service.getServiceHandle());
		  if(service1==service) {
			  return entry.getValue();
		  }
	  }
	  return null;
  }
  
  private static Object createRemotes(Handle providerId, Handle serviceId, Class<? extends Service> type) throws CoreException {
    if (type != null) {
      RemoteServiceInvoker invoker = new RemoteServiceInvoker(providerId, serviceId, Scope.getServiceHost(), Scope.getServicePort());
      Object proxy = Proxy.newProxyInstance(Services.class.getClassLoader(), new Class[] { type }, invoker);
      ServiceProvider<Service> provider0 = (ServiceProvider)provider.get(DEFAULT);
      provider0.bind((Service)proxy);
      return proxy;
    } 
    CoreException.throwCoreException("To get a remote Service, the type must be known");
    return null;
  }
  
  private static Method findMethod(Class<?> clazz, String methodName, Set<String> parameter) {
    try {
      Method method = null;
      method = findMethod0(clazz, methodName, parameter);
      if (method != null)
        return method; 
      if (method == null && clazz.getSuperclass() != null)
        method = findMethod(clazz.getSuperclass(), methodName, parameter); 
      if (method == null) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> _interface : interfaces) {
          method = findMethod(_interface, methodName, parameter);
          if (method != null)
            return method; 
        } 
      } 
      return method;
    } catch (Throwable t) {
      return null;
    } 
  }
  
  private static Method findMethod0(Class<?> clazz, String methodName, Set<String> parameter) {
    Method[] methods = clazz.getDeclaredMethods();
    for (Method m : methods) {
      Parameter[] methodParams = m.getParameters();
      Function functionAnn = m.<Function>getAnnotation(Function.class);
      if (functionAnn != null) {
        String mName = (functionAnn.value().length() > 0) ? functionAnn.value() : m.getName();
        if (mName.equals(methodName) && methodParams.length == parameter.size()) {
          boolean foundAllParameter = true;
          for (Parameter p : methodParams) {
            Param paramAnno = p.<Param>getAnnotation(Param.class);
            if (!parameter.contains(paramAnno.value())) {
              foundAllParameter = false;
              break;
            } 
          } 
          if (foundAllParameter)
            return m; 
        } 
      } 
    } 
    return null;
  }
  
  private static Object[] toArgsArray(Method m, Map<String, Object> parameter) throws CoreException {
    Parameter[] methodParams = m.getParameters();
    Object[] args = new Object[methodParams.length];
    for (int i = 0; i < methodParams.length; i++) {
      Param param = methodParams[i].<Param>getAnnotation(Param.class);
      Object o = parameter.get(param.value());
      args[i] = o;
    } 
    return args;
  }
  
  public static Object invoke(Call call) throws CoreException {
    try {
      return getInvoker(call).invoke(call);
    } catch (Throwable t) {
      throw CoreException.throwCoreException(t);
    } 
  }
  
  private static Invoker getInvoker(Call call) throws CoreException {
    if (Scope.getScope() == Scope.LOCAL)
      return localInvoker; 
    return new RemoteServiceInvoker(call.getProvider(), call
        .getService(), 
        Scope.getServiceHost(), 
        Scope.getServicePort());
  }
}
