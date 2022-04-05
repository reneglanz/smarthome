package de.shd.device;

import java.util.List;

import de.core.CoreException;
import de.core.service.Function;
import de.core.service.Param;
import de.shd.device.data.ImageData;
import de.shd.device.data.StreamData;

public interface Camera {

	public enum InfraRedState {
		ON,
		OFF,
		AUTO
	}
	@Function public State setRecordingState(@Param("state") State state) throws CoreException;
	@Function public State getRecordingState() throws CoreException;
	@Function public State toggleRecording() throws CoreException;
	@Function public void setInfraRedState(@Param("irState") InfraRedState irState) throws CoreException;
	@Function public InfraRedState getInfraRedState() throws CoreException;
	@Function public ImageData getImage() throws CoreException;
	@Function public StreamData getStream() throws CoreException;
	@Function public List<Recording> getRecordings() throws CoreException;
	@Function public void deleteRecording(@Param("recording") Recording recording) throws CoreException;
	@Function public void deleteAllRecordings() throws CoreException;
}

