package de.shd.volume;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.core.service.Service;

public interface VolumeService extends Service {
	
	@Function public int getVolume() throws CoreException; 
	
	@Function public void setVolume(@Param("value") int value) throws CoreException;
	
	@Function public void mute() throws CoreException;
	
	@Function public void unmute() throws CoreException;
}
