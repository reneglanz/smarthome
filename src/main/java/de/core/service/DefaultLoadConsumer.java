package de.core.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.core.CoreException;
import de.core.Env;
import de.core.log.Logger;
import de.core.rt.Launchable;
import de.core.task.Scheduler;
import de.core.task.Task;

public class DefaultLoadConsumer implements BiConsumer<Path,Object>, Launchable {
	protected Logger logger = Logger.createLogger("LoadConsumer");

	protected Scheduler taskExecutor;

	protected List<Launchable> toLaunch = new ArrayList<>();

	protected boolean directLaunch = false;

	protected ServiceProvider provider;

	public DefaultLoadConsumer() {
		this.taskExecutor = (Scheduler) Env.get(Scheduler.class);
	}

	public DefaultLoadConsumer(ServiceProvider provider, boolean directLaunch) {
		this.provider = provider;
		this.directLaunch = directLaunch;
	}

	public void accept(Path p,Object loaded) {
		if( loaded instanceof de.core.serialize.Enclosure) try {
			Env.put(loaded);
		} catch (CoreException e) {
			this.logger.error("Ressource  " + loaded.getClass() + " already defined", (Throwable)e);
		}  
		
		if(loaded instanceof de.core.rt.Resource) try {
			Env.put(((de.core.rt.Resource) loaded).getName(), loaded);
		} catch (CoreException e) {
			this.logger.error("Ressource  " + loaded.getClass() + " already defined", (Throwable)e);
		}  
		
	    if(loaded instanceof Launchable) {
	    	if(this.directLaunch) try {
	    		((Launchable)loaded).launch();
	        } catch (CoreException e) {
	        	this.logger.error("Failed to launch " + loaded.getClass());
	        } else {
	        	this.toLaunch.add((Launchable)loaded);
	        }  
	    }
	    if(loaded instanceof ServiceProvider) {
	    	Services.addProvider((ServiceProvider)loaded);
	    }
	    if(loaded instanceof Service) try {
	    	if (this.provider != null) {
	    		this.provider.bind((Service)loaded);
	    	} else {
	    		Services.bind((Service)loaded);
	    	} 
	    } catch (CoreException e) {
	        this.logger.error("Failed to register " + loaded.getClass() + " as Service");
	    }  
    if (loaded instanceof Task)
      this.taskExecutor.schedule((Task)loaded); 
  }

	public void launch() throws CoreException {
		this.toLaunch.forEach(l -> {
			try {
				l.launch();
			} catch (CoreException e) {
				this.logger.error("Failed to launch " + l.getClass(), (Throwable) e);
			}
		});
	}
}
