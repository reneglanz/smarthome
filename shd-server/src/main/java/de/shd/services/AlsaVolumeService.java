package de.shd.services;

import de.core.serialize.annotation.Element;
import de.shd.volume.VolumeService;

public class AlsaVolumeService implements VolumeService {

	@Element protected String mixer;
	
	@Override
	public int getVolume() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVolume(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unmute() {
		// TODO Auto-generated method stub
		
	}

}
