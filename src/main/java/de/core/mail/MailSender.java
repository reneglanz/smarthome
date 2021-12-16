package de.core.mail;

import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import de.core.log.Logger;
import de.core.rt.Resource;
import de.core.serialize.annotation.Element;
import de.core.service.Function;

public class MailSender implements Resource {
	
	Logger logger=Logger.createLogger("MailSender");
	@Element protected String smtpHost;
	@Element protected int port;
	@Element protected String from;
	@Element protected String user;
	@Element protected String password;
	@Element protected boolean starttls;
	@Element protected Map<String,String> props;
	
	@Function
	public void send(String mailTo,String subject, String content, byte[] attachement, String attachmentType, String attachementFileName) {
	      Properties mailprops = new Properties();
	      mailprops.put("mail.smtp.host", smtpHost);
	      mailprops.put("mail.smtp.port", "25");
	      mailprops.put("mail.smtp.timeout", 20*1000);
	      mailprops.put("mail.smtp.connectiontimeout", 20*1000);
	      if(starttls) {
	    	  mailprops.put("mail.smtp.starttls.enable", "true");
	      }
	      
	      if(this.props!=null) {
	    	  mailprops.putAll(this.props);
	      }
	      
	      Session session=null;
	      if(password!=null) {
		      mailprops.put("mail.smtp.auth", "true");
		      PasswordAuthentication auth=new PasswordAuthentication(user, password);
		      session= Session.getInstance(mailprops,new javax.mail.Authenticator() {
		    	  protected PasswordAuthentication getPasswordAuthentication() {
		    		  return auth;
		    	  }
		      });
	      } else {
	    	  session=Session.getInstance(mailprops);
	      }
	      

	      try {
	         Message message = new MimeMessage(session);
	         message.setFrom(new InternetAddress(from));
	         message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(mailTo));
	         message.setSubject(subject);

	         Multipart multipart = new MimeMultipart();
	         if(content!=null) {
		         // Create the message part
		         BodyPart messageBodyPart = new MimeBodyPart();
		         messageBodyPart.setText(content);
		         multipart.addBodyPart(messageBodyPart);
	      	}	

	         if(attachement!=null) {
	        	 MimeBodyPart messageBodyPart = new MimeBodyPart();
	        	 DataSource source = new ByteArrayDataSource(attachement, attachmentType);
	        	 messageBodyPart.setDataHandler(new DataHandler(source));
	        	 messageBodyPart.setFileName(attachementFileName);
	        	 multipart.addBodyPart(messageBodyPart);
	         }

	         // Send the complete message parts
	         message.setContent(multipart);

	         // Send message
	         Transport.send(message);
	         logger.debug("Mail sent. subject: "+subject +" to " + mailTo);
	         System.out.println("Sent message successfully....");
	  
	      } catch (MessagingException e) {
	         logger.error("Failed to sent mail. subject: "+subject +" to " + mailTo, e);
	      }
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
