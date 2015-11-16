import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.mail.internet.InternetAddress;
import java.io.FileWriter;
import javax.mail.MessagingException;
import javax.mail.Message;

public class IncomingMail_Trans {
	public static String logMessage (Message msg) {
	FileWriter fw;
        try {  
            fw = new FileWriter("mail_log.txt",true);
    	    fw.append("******************************************");
	    fw.append(System.lineSeparator());
	    DateFormat df = new SimpleDateFormat("MMM dd,yyyy HH:mm");
	    Date date = new Date();
	    fw.append(df.format(date));
            fw.append(System.lineSeparator());
	    fw.append(InternetAddress.toString(msg.getFrom()));
	    fw.append(System.lineSeparator()); // new line
	    fw.append(InternetAddress.toString(msg.getFrom()));
	    fw.append(System.lineSeparator());
	    fw.append(System.lineSeparator());
	    fw.append("ContentType: " + msg.getContentType());
	    fw.append(System.lineSeparator());
	    fw.append("Content: " + msg.getContent().toString());
	    fw.append(System.lineSeparator());
            fw.close();		
	} catch (Exception e) {
	    System.out.println("file I/O error"); 
	}
	System.out.println("done logMessage");
        return "done logMessage";
    }
	public static String spamifySubject(String emailSubject) throws MessagingException {
	System.out.println("spamifySubjecting email....");
        return "done spamifySubject";
    }
}