package de.shd.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.serialize.Coding;
import de.core.service.LocalServiceProvider;
import de.core.utils.Levenshtein;
import de.shd.ui.Editable;

public class DeviceProvider extends LocalServiceProvider<AbstractDevice> implements Iterable<AbstractDevice>,Editable {
	
	public AbstractDevice get(String name, boolean caseSensitive) throws CoreException {
		for (AbstractDevice widget : this) {
			if (caseSensitive ? widget.getName().equals(name) : widget.getName().equalsIgnoreCase(name))
				return widget;
		}
		return null;
	}

	public AbstractDevice get(String name) throws CoreException {
		return get(name, true);
	}

	public AbstractDevice bestMatch(String name) throws CoreException {
		AbstractDevice device = get(name, false);
		if (device == null) {
			int best = Integer.MAX_VALUE;
			for (AbstractDevice d : this) {
				int tmp = Levenshtein.compare(name, d.name).intValue();
				if (tmp < best) {
					best = tmp;
					device = d;
				}
			}
		}
		return device;
	}

	public AbstractDevice getService(Handle id) throws CoreException {
		return (AbstractDevice) super.getService(id);
	}

	public Iterator<AbstractDevice> iterator() {
		return new Iterator<AbstractDevice>() {
			Iterator<Map.Entry<Handle, AbstractDevice>> it = DeviceProvider.this.services.entrySet().iterator();

			public AbstractDevice next() {
				return (AbstractDevice) ((Map.Entry) this.it.next()).getValue();
			}

			public boolean hasNext() {
				return this.it.hasNext();
			}
		};
	}

	public Handle getServiceHandle() {
		return (Handle) this.providerId;
	}

	@Override
	public List<Handle> list() throws CoreException {
		ArrayList<Handle>list=new ArrayList<>();
		this.forEach(device->{list.add(new NameHandle(device.name));});
		return list;
	}

	@Override
	public String get(Handle handle) throws CoreException {
		for(AbstractDevice device:this) {
			if(handle instanceof NameHandle && device.getName().equals(((NameHandle)handle).toString())) {
				return Coding.toBase64(Coding.encode(device));
			}
		}
		return null;
	}

	@Override
	public void update(String content) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(String content) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String template(String name) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
