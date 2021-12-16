package de.shd.automation;

import de.core.CoreException;
import de.core.handle.NameHandle;
import de.core.serialize.annotation.Injectable;
import de.shd.automation.trigger.AbstractTrigger;
import de.shd.automation.trigger.DeviceTrigger;
import de.shd.device.ExportData;
import de.shd.update.UpdateService;

public class AutomationUpdateListener implements UpdateService.UpdateListener {

	@Injectable AutomationStore store;
	
	@Override
	public void onUpdate(ExportData data) throws CoreException {
		if(data instanceof ExportData) {
			ExportData exportData=(ExportData)data;
			for(Automation automation:store.automations) {
				for(AbstractTrigger trigger:automation.trigger) {
					if(trigger instanceof DeviceTrigger
					   &&((NameHandle)(exportData).getDeviceHandle()).toString().equals(((DeviceTrigger) trigger).getDeviceId().toString())) {
						trigger.runAutomation();
						break;
					}
				}
			}
		}
		
	}

	
}
