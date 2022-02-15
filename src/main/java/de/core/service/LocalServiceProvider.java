package de.core.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.Env;
import de.core.log.Logger;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.rt.Reloadable;
import de.core.serialize.annotation.Element;

public class LocalServiceProvider<E extends Service> implements ServiceProvider<E>, Releasable, Launchable, Reloadable {
	@Element protected String providerId;
	protected Map<String, E> services = Collections.synchronizedMap(new HashMap<>());
	private static Logger logger = Logger.createLogger("Services");
	
	protected LocalServiceProvider() {
	}

	public LocalServiceProvider(String providerId) {
		this.providerId = providerId;
	}

	public void bind(E service) throws CoreException {
		this.services.put(service.getServiceHandle(), service);
    	logger.info("Bound service "+service.getServiceHandle()+"["+service.getClass().toString()+"] to " + providerId.toString());
	}

	public String getProviderId() {
		return this.providerId;
	}

	public E getService(String id) throws CoreException {
		return this.services.get(id);
	}

	public void unbind(E service) throws CoreException {
		this.services.remove(service.getServiceHandle());
    	logger.info("Unbind service "+service.getServiceHandle()+"["+service.getClass().toString()+"] from " + providerId.toString());
	}

	@Override
	public void unbind(String handle) throws CoreException {
		this.services.remove(handle);
    	logger.info("Unbind service "+handle.toString()+" from " + providerId.toString());

	}

	@SuppressWarnings("unchecked")
	public E getService(Class<E> clazz) throws CoreException {
		Service service = (Service) getService(clazz.getName());
		if (service == null) {
			for (Map.Entry<String, E> entry : this.services.entrySet()) {
				if (((Service) entry.getValue()).implements0(clazz))
					return entry.getValue();
			}
		}
		return (E) service;
	}

	public void release() throws CoreException {
		for (Map.Entry<String, E> entry : this.services.entrySet()) {
			if (entry.getValue() instanceof Releasable) {
				((Releasable) entry.getValue()).release();
			}
		}
		this.services.clear();
	}

	@Override
	public void launch() throws CoreException {
		load();
	}
	
	public void load() throws CoreException {
		Loader<Service> loader=new Loader<>(Paths.get(Env.get("install.dir"),"config",providerId.toString()));
		try {
			DefaultLoadConsumer consumer=new DefaultLoadConsumer(this, false);
			loader.load(consumer);
			consumer.launch();
		} catch (IOException e) {
			CoreException.throwCoreException(e);
		}
	}

	@Override
	public void reload() throws CoreException {
		release();
		load();
	}
}
