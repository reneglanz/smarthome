package de.shd.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.core.CoreException;
import de.core.rt.Launchable;
import de.core.serialize.annotation.Element;
import de.core.service.Service;
import de.core.service.ServiceProvider;
import de.core.service.Services;
import de.shd.device.data.SwitchData;
import de.shd.update.UpdateService;

public class ContainerDevice extends AbstractDevice 
	implements Launchable,Switch,Light,Shutter,Range,UpdateService {

	public enum FollowMode {
		LEADER,
		DYNAMIC
	}
	
	@Element String leader;
	@Element List<String> follower; 
	@Element String serviceProvider;
	@Element String[] services;
	@Element(defaultValue="LEADER") FollowMode followMode=FollowMode.LEADER;
	
	protected List<Service> devices=new ArrayList<Service>();
	
	@Override
	public ExportData createExportData() {
		return ((AbstractDevice)getLeader()).createExportData();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void launch() throws CoreException {
		ServiceProvider provider=Services.getProvider(serviceProvider);
		Service s=provider.getService(leader);
		if(s!=null) {
			Service sLeader=provider.getService(leader);
			if(sLeader instanceof AbstractDevice) {
				((AbstractDevice) sLeader).setUpdateService(this);
			}
			devices.add(sLeader);
			follower.forEach(f->{
				try {
					Service sf=provider.getService(f);
					if(sf!=null) {
						if(sf instanceof AbstractDevice) {
							((AbstractDevice) sf).setUpdateService(this);
						}
						devices.add(sf);
					}
				} catch (CoreException e) {
				}
			});
		} else {
			CoreException.throwCoreException("["+this.getDeviceId()+"]"+"Leader not found " + leader);
		}
	}

	@Override
	public State toggle() throws CoreException {
		if(getLeader()instanceof Toggle) {
			if(followMode==FollowMode.LEADER) {
				State state0=((Toggle)getLeader()).toggle();
				for(Service d:devices) {
					if(!d.getServiceHandle().equals(getLeader().getServiceHandle())
					   && d instanceof Switch) {
						((Switch)d).setState(state0);
					}
				}
				return state0;
			} else if(followMode==FollowMode.DYNAMIC) {
				State state0=State.OFF;
				for(Service d:devices) if(d instanceof Switch){
					if(((Switch) d).getState()==State.ON) {
						state0=State.ON;
						break;
					}
				}
				State finalState=state0==State.ON?State.OFF:State.ON;
				for(Service d:devices) if(d instanceof Switch){
					((Switch)d).setState(finalState);
				}
				return finalState;
			}
		}
		return State.UNKNOWN;
	}

	@Override
	public void setRange(int value) throws CoreException {
		if(devices.get(0) instanceof Range) {
			for(Service d:devices) {
				((Range)d).setRange(value);
			}
		}
	}

	@Override
	public int getRange() throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void open() throws CoreException {
		if(devices.get(0) instanceof Shutter) {
			for(Service d:devices) {
				((Shutter)d).open();
			}
		}
	}

	@Override
	public void close() throws CoreException {
		if(devices.get(0) instanceof Shutter) {
			for(Service d:devices) {
				((Shutter)d).close();
			}
		}
	}

	@Override
	public void stop() throws CoreException {
		if(getLeader() instanceof Shutter) {
			for(Service d:devices) {
				((Shutter)d).stop();
			}
		}
	}

	@Override
	public void setBrightness(int paramInt) throws CoreException {
		if(getLeader() instanceof Light) {
			for(Service d:devices) {
				((Light)d).setBrightness(paramInt);
			}
		}
	}

	@Override
	public void setColorTemp(int paramInt) throws CoreException {
		if(getLeader() instanceof Light) {
			for(Service d:devices) {
				((Light)d).setColorTemp(paramInt);
			}
		}
	}

	@Override
	public void setColor(Color paramColor) throws CoreException {
		if(getLeader() instanceof Light) {
			for(Service d:devices) {
				((Light)d).setColor(paramColor);
			}
		}
	}

	@Override
	public int getBrightness() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).getBrightness();
		}
		return 0;
	}

	@Override
	public int getColorTemp() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).getColorTemp();
		}
		return 0;
	}

	@Override
	public boolean canDim() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).canDim();
		}
		return false;
	}

	@Override
	public boolean canColorTemp() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).canColorTemp();
		}
		return false;
	}

	@Override
	public boolean canColor() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).canColor();
		}
		return false;
	}

	@Override
	public List<Color> getColorPresets() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).getColorPresets();
		}
		return Collections.emptyList();
	}

	@Override
	public Color getColor() throws CoreException {
		if(getLeader()instanceof Light) {
			return ((Light)getLeader()).getColor();
		}
		return null;
	}

	@Override
	public State getState() throws CoreException {
		if(getLeader()instanceof Switch) {
			if(followMode==FollowMode.LEADER) {
				return ((Switch)getLeader()).getState();
			} else if(followMode==FollowMode.DYNAMIC) {
				State state0=State.OFF;
				for(Service d:devices) {
					if(((Switch) d).getState()==State.ON) {
						state0=State.ON;
						break;
					}
				}
				return state0;
			}
		}
		return State.UNKNOWN;
	}

	@Override
	public State setState(State paramState) throws CoreException {
		if(getLeader() instanceof Switch) {
			State state=null;
			for(Service d:devices) {
				State state0=((Switch)d).setState(paramState);
				if(state==null) {
					state=state0;
				}
			}
			return state;
		}
		return State.UNKNOWN;
	}

	
	public Service getLeader() {
		if(devices!=null&&devices.size()>=1) {
			return devices.get(0);
		}
		return null;
	}

	@Override
	public void register(UpdateListener listener) throws CoreException {
		CoreException.throwCoreException("Not supported");
	}

	@Override
	public void update(ExportData data) throws CoreException {
		if (this.updateService == null) {
			this.updateService = (UpdateService) Services.get(UpdateService.class);
		}
		if (this.updateService != null) {
			this.updateService.update(data);
			if(followMode==FollowMode.LEADER) {
				if(data.device.equals(this.leader)) {
					this.updateService.update(new ExportData(getDeviceId(), this.name, data.data));
				}
			} else if(followMode==FollowMode.DYNAMIC) {
				this.updateService.update(new ExportData(getDeviceId(), this.name, new SwitchData(this.getState())));
			}
		}
	}
}
