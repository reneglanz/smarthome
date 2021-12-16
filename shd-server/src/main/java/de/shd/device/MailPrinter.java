package de.shd.device;

import de.core.mail.MailSender;
import de.core.serialize.annotation.Element;
import de.core.serialize.annotation.Injectable;
import de.core.service.Function;
import de.shd.device.data.TextData;

public class MailPrinter extends AbstractDevice {
	
	@Injectable MailSender mailSender;
	@Element protected String to;

	@Function
	public void print() {
	}
	
	@Override
	public ExportData createExportData() {
		return new ExportData(getDeviceHandle(), name, new TextData(""));
	}

	public static void main(String[] args) {
//		MailPrinter printer=new MailPrinter();
//		printer.from="rene.glanz@googlemail.com";
//		printer.to="pnv8458ww6grc5@print.epsonconnect.com";
//		printer.user="rene.glanz@googlemail.com";
//		printer.password="vfvlocgvidezctyw";
//		printer.usessl=true;
//		printer.port=587;
//		printer.smtp="smtp.googlemail.com";	
//		printer.print();
	}
}
