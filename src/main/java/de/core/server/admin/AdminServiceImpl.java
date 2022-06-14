package de.core.server.admin;

import de.core.CoreException;
import de.core.Env;
import de.core.log.Logger;
import de.core.server.Server;
import de.core.service.ServiceDescription;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.core.task.Scheduler;
import java.util.Collections;
import java.util.List;

public class AdminServiceImpl implements AdminService {
	
	protected Server server;

	public AdminServiceImpl(Server server) {
		this.server = server;
	}

	public void shutdown() throws CoreException {
		this.server.shutdown();
	}

	public List<Scheduler.ExecutionPlanEntry> schedules() throws CoreException {
		Scheduler scheduler = (Scheduler) Env.get(Scheduler.class);
		if (scheduler != null)
			return scheduler.getExcutionPlan();
		return Collections.emptyList();
	}

	public void setLogLevel(int level) throws CoreException {
		Logger.setRootLogLevel(level);
	}

	public String getServiceHandle() {
		return "admin";
	}

	@Override
	public List<String> provider() throws CoreException {
		return Services.getProviderNames();
	}

	@Override
	public List<String> services(String provider) throws CoreException {
		if(provider==null) {
			provider=Services.DEFAULT;
		}
		ServiceProvider<?> serviceProvider=Services.getProvider(provider);
		if(serviceProvider!=null) {
			return serviceProvider.getServiceIds();
		} else {
			throw new CoreException("No such provider " + provider);
		}
	}
	
	public ServiceDescription describeService(String provider, String service) throws CoreException {
		if(provider==null) {
			provider=Services.DEFAULT;
		}
		ServiceProvider<?> serviceProvider=Services.getProvider(provider);
		if(serviceProvider!=null) {
			return ServiceDescription.forService(serviceProvider.getService(service));
		} else {
			throw new CoreException("No such provider " + provider);
		}
	}

	@Override
	public String getEndpointUrl(String connector) throws CoreException {
		return server.getConnectorUrl(connector);
	}
}
