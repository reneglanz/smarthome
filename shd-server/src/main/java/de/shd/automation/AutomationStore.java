package de.shd.automation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.core.CoreException;
import de.core.Env;
import de.core.log.Logger;
import de.core.rt.Activatable;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.rt.Reloadable;
import de.core.rt.Resource;
import de.core.serialize.Coding;
import de.core.service.DefaultLoadConsumer;
import de.core.service.Loader;
import de.shd.ui.Editable;

public class AutomationStore implements Resource, Launchable, Releasable, Reloadable, Editable, Activatable {
	List<Automation> automations;
	List<Automation> deactivatedAutomations;
	Logger logger=Logger.createLogger("AutomationStore");
	public void release() throws CoreException {
		if (this.automations != null) {
			this.automations.forEach(a -> {
				if (a instanceof Releasable)
					try {
						a.release();
					} catch (CoreException coreException) {
					}
			});
			this.automations.clear();
		}
		this.automations = Collections.synchronizedList(new ArrayList<>());
		this.deactivatedAutomations = Collections.synchronizedList(new ArrayList<>());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void load() throws CoreException {
		release();
		try {
			new Loader(Paths.get(Env.get("install.dir") + "/config/automations", new String[0]))
					.load(new DefaultLoadConsumer(null, true) {
						public void accept(Path p,Object loaded) {
							super.accept(p,loaded);
							if (loaded instanceof Automation) {
								AutomationStore.this.logger.info("Loaded Automation " + loaded.toString());
								((Automation)loaded).setFile(p.toString());
								AutomationStore.this.automations.add((Automation) loaded);
							}
						}
					});
			
			if(Files.exists(Paths.get(Env.get("install.dir") + "/config/automations/deactivated"))){
				new Loader(Paths.get(Env.get("install.dir") + "/config/automations/deactivated", new String[0]))
				.load(new DefaultLoadConsumer(null, true) {
					public void accept(Path p,Object loaded) {
						super.accept(p,loaded);
						if (loaded instanceof Automation) {
							AutomationStore.this.logger.info("Loaded Automation " + loaded.toString());
							((Automation)loaded).setFile(p.toString());
							AutomationStore.this.deactivatedAutomations.add((Automation) loaded);
						}
					}
				});
			}
			
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
	}

	public void launch() throws CoreException {
		load();
	}

	public void reload() throws CoreException {
		release();
		load();
	}

	public String getServiceHandle() {
		return "automationstore";
	}

	@Override
	public List<String> list() throws CoreException {
		ArrayList<String> handle=new ArrayList<>();
		automations.forEach(a->{
			handle.add(a.getId());
		});
		deactivatedAutomations.forEach(a->{
			handle.add(a.getId() + " - deaktiviert");
		});
		return handle;
	}
	
	public Automation get(List<Automation> automations,String handle) throws CoreException {
		for(Automation a: automations) {
			if(a.getId().equals(handle)){
				return a;
			}
		}
		return null;
	}

	@Override
	public String get(String handle) throws CoreException {
		try {
			Automation a=get(this.automations,handle);
			if(a==null) {
				a=get(this.deactivatedAutomations,handle);
			}
			if(a!=null) {
				return Coding.toBase64(Coding.encode(a));
			}
		} catch (Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}

	@Override
	public void update(String content) throws CoreException {
		Automation tosave=Coding.decode(Coding.fromBase64(content));
		Automation toUpdate=null;
		for(Automation saved:automations) {
			if(saved.id.equals(tosave.id)) {
				toUpdate=saved;
				break;
			}
		}
		if(toUpdate!=null) {
			try {
				Files.write(Paths.get(toUpdate.getFile()), Coding.encode(tosave));
			} catch (Throwable t) {
				CoreException.throwCoreException(t);
			}
		}
		reload();
	}

	@Override
	public void add(String content) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String template(String name) throws CoreException {
		String template ="{\r\n"
				+ "	@type=de.shd.automation.Automation\r\n"
				+ "	id=${ID}\r\n"
				+ "	name=${NAME}\r\n"
				+ "	trigger=[]\r\n"
				+ "	conditions=[]\r\n"
				+ "	than=[]\r\n"
				+ "	else=[]\r\n"
				+ "}";
		template=template.replace("${NAME}", name).replace("${ID}", name);
		return Coding.toBase64(template.getBytes());		
	}

	@Override
	public void activate(String serviceId) throws CoreException {
		Automation automation=get(this.deactivatedAutomations,serviceId);
		if(automation!=null) {
			Path deactivated=Paths.get(automation.getFile());
			Path path=Paths.get(deactivated.getParent().getParent().toString(),deactivated.getFileName().toString());
			try {
				Files.move(deactivated, path);
				automation.setFile(path.toString());
				this.deactivatedAutomations.remove(automation);
				this.automations.add(automation);
			} catch(IOException e) {
				CoreException.throwCoreException(e);
			}
		}
	}

	@Override
	public void deactivate(String serviceId) throws CoreException {
		Automation automation=get(this.automations,serviceId);
		if(automation!=null) {
			automations.remove(automation);
			Path path=Paths.get(automation.getFile());
			Path deactivated=Paths.get(path.getParent().toString(),"deactivated",path.getFileName().toString());
			try {
				Files.move(path,deactivated);
				automation.setFile(deactivated.toString());
				this.deactivatedAutomations.add(automation);
			} catch(IOException e) {
				CoreException.throwCoreException(e);
			}
		}
	}
}
