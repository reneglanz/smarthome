package de.shd.automation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.core.CoreException;
import de.core.Env;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.rt.Launchable;
import de.core.rt.Releasable;
import de.core.rt.Reloadable;
import de.core.rt.Resource;
import de.core.serialize.Coding;
import de.core.service.DefaultLoadConsumer;
import de.core.service.Loader;
import de.shd.ui.Editable;

public class AutomationStore implements Resource, Launchable, Releasable, Reloadable, Editable {
	List<Automation> automations;

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
								((Automation)loaded).setFile(p.toString());
								AutomationStore.this.automations.add((Automation) loaded);
							}
						}
					});
		} catch (Throwable t) {
			throw CoreException.throwCoreException(t);
		}
	}

	public void launch() throws CoreException {
		load();
	}

	public void reload() throws CoreException {
		release();
		load();
	}

	public NameHandle getServiceHandle() {
		return new NameHandle("automationstore");
	}

	@Override
	public List<Handle> list() throws CoreException {
		ArrayList<Handle> handle=new ArrayList<>();
		automations.forEach(a->{
			handle.add(new NameHandle(a.getId()));
		});
		return handle;
	}

	@Override
	public String get(Handle handle) throws CoreException {
		try {
			if(handle instanceof NameHandle) {
				NameHandle name=(NameHandle) handle;
				for(Automation a: automations) {
					if(a.getId().equals(name.toString())){
						return Coding.toBase64(Coding.encode(a));
					}
				}
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
}