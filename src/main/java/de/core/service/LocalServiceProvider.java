package de.core.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.Env;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.rt.Reloadable;
import de.core.serialize.annotation.Element;

public class LocalServiceProvider<E extends Service> implements ServiceProvider<E>, Releasable, Launchable, Reloadable {
	@Element(inline = true) protected NameHandle providerId;
	protected Map<Handle, E> services = Collections.synchronizedMap(new HashMap<>());
	
	protected LocalServiceProvider() {
	}

	public LocalServiceProvider(NameHandle providerId) {
		this.providerId = providerId;
	}

	public void bind(E service) throws CoreException {
		this.services.put(service.getServiceHandle(), service);
	}

	public Handle getProviderId() {
		return (Handle) this.providerId;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public E getService(Handle id) throws CoreException {
		if (id instanceof NameHandle) {
			String name = id.toString();
			try {
				Class clazz = Class.forName(name);
				E service = (E) getService(clazz);
				if (service != null) {
					return service;
				}
			} catch (Throwable t) {
			}
		}
		return this.services.get(id);
	}

	public void unbind(E service) throws CoreException {
		this.services.remove(service.getServiceHandle());
	}

	@Override
	public void unbind(Handle handle) throws CoreException {
		this.services.remove(handle);
	}

	@SuppressWarnings("unchecked")
	public E getService(Class<E> clazz) throws CoreException {
		Service service = (Service) getService((Handle) new NameHandle(clazz.getName()));
		if (service == null) {
			for (Map.Entry<Handle, E> entry : this.services.entrySet()) {
				if (((Service) entry.getValue()).implements0(clazz))
					return entry.getValue();
			}
		}
		return (E) service;
	}

	public void release() throws CoreException {
		for (Map.Entry<Handle, E> entry : this.services.entrySet()) {
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
			loader.load(new DefaultLoadConsumer(this, true));
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
