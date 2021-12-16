package de.shd.services;

import java.util.ArrayList;
import java.util.List;

import de.core.CoreException;
import de.core.log.Logger;
import de.core.serialize.annotation.Element;
import de.shd.device.ExportData;
import de.shd.update.UpdateService;

public class UpdateServiceImpl implements UpdateService {
	Logger logger=Logger.createLogger("UpdateServiceImpl");
	@Element List<UpdateListener> listener=new ArrayList<>();
	
	@Override
	public void register(UpdateListener listener) throws CoreException {
		this.listener.add(listener);
	}

	@Override
	public void update(ExportData data) throws CoreException {
		listener.forEach(l->{
			try {
				l.onUpdate(data);
			} catch (CoreException e) {
				logger.error("Failed to send update data to "+l.toString());
			}
		});
	}
}
