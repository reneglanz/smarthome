package de.shd.automation.action;

import de.core.CoreException;
import de.core.handle.Handle;
import de.core.handle.NameHandle;
import de.core.serialize.annotation.Element;
import de.core.service.Service;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.shd.automation.Data;
import de.shd.device.Shutter;

public class ShutterPositionAction implements Action {
	
	@Element(inline = true) protected NameHandle provider = null;
	@Element(inline = true) protected NameHandle service = null;
	@Element(defaultValue="0") protected int pos=0;
	
	private Shutter shutter0;
	
	@Override
	public void execute(Data paramData) throws CoreException {
		Shutter shutter=getShutter();
		shutter.setRange(pos);
	}

	protected Shutter getShutter() {
		if(shutter0==null) {
	      ServiceProvider<? extends Service> serviceprovider = Services.getProvider((Handle)this.provider);
	      if (serviceprovider != null) {
	        try {
	          Service service0 = serviceprovider.getService((Handle)this.service);
	          if (service0 != null && service0 instanceof Shutter) {
	            this.shutter0 = (Shutter)service0;
	          } else {
	            this.shutter0 = new ShutterNotFound();
	          } 
	        } catch (CoreException e) {
	          e.printStackTrace();
	        } 
	      } else {
	        this.shutter0 =  new ShutterNotFound();;
	      } 
		}
		return shutter0;
	}
}
