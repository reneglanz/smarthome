package de.shd.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.core.CoreException;
import de.core.serialize.Coding;
import de.core.service.LocalServiceProvider;
import de.shd.ui.Editable;

public class DeviceProvider extends LocalServiceProvider<AbstractDevice> implements Iterable<AbstractDevice>,Editable {
	
	public AbstractDevice get(String name, boolean caseSensitive) throws CoreException {
		for (AbstractDevice widget : this) {
			if (caseSensitive ? widget.getName().equals(name) : widget.getName().equalsIgnoreCase(name))
				return widget;
		}
		return null;
	}

	public AbstractDevice getService(String id) throws CoreException {
		return (AbstractDevice) super.getService(id);
	}

	public Iterator<AbstractDevice> iterator() {
		return new Iterator<AbstractDevice>() {
			Iterator<Map.Entry<String, AbstractDevice>> it = DeviceProvider.this.services.entrySet().iterator();

			public AbstractDevice next() {
				return (AbstractDevice) ((Map.Entry) this.it.next()).getValue();
			}

			public boolean hasNext() {
				return this.it.hasNext();
			}
		};
	}

	public String getServiceHandle() {
		return this.providerId;
	}

	@Override
	public List<String> list() throws CoreException {
		ArrayList<String>list=new ArrayList<>();
		this.forEach(device->{list.add(device.name);});
		return list;
	}

	@Override
	public String get(String handle) throws CoreException {
		for(AbstractDevice device:this) {
			if(device.getName().equals(handle)) {
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
