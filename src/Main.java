import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String SMTP_SERVER_HOST = "smtp.gmail.com";
    private static final String SMTP_SERVER_PORT = "587";
    private static final String SUBJECT = "[ERROR]: System info";
    private static final String BODY = "Server is not reachable";

    public static void main(String[] args) throws MessagingException {

        final String FROM_USER_EMAIL = args[0];
        final String FROM_USER_ACCESSTOKEN = args[1];
        final String TO_USER_EMAIL = args[2];

        boolean is_not_reachable;
        Date old_date = new Date();
        String ip_server = "192.168.1.102";
        is_not_reachable = checkIpStatus(ip_server);

        while (true){
            Date new_date = new Date();
            long duration  = new_date.getTime() - old_date.getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);

            if (diffInMinutes > 1){
                System.out.println("Ok " + new_date);
                if (is_not_reachable) {
                    System.out.println(is_not_reachable + " " + old_date);
                    sendMail(SMTP_SERVER_HOST, SMTP_SERVER_PORT, FROM_USER_EMAIL, FROM_USER_ACCESSTOKEN,
                            TO_USER_EMAIL, SUBJECT, BODY);
                }
                old_date = new Date();
            }
        }
    }

    public static boolean checkIpStatus(String ipAddress) {
        boolean reachable = false;
        try {
            reachable = InetAddress.getByName(ipAddress).isReachable(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reachable;
    }

    public static void sendMail(
            String smtpServerHost,  String smtpServerPort,  String fromUserEmail, String smtpUserAccessToken,
            String toEmail, String subject, String body) throws MessagingException {

        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromUserEmail, smtpUserAccessToken);
            }
        };
        Date date = new Date();

        Transport transport = null;
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpServerHost);
            props.put("mail.smtp.port", smtpServerPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "*");

            Session session = Session.getDefaultInstance(props, auth);
//            session.setDebug(true);

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromUserEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(subject + date);
            msg.setContent(body, "text/html");

            transport = session.getTransport("smtp");
            transport.connect();
            Transport.send(msg);

        } catch (MessagingException e) {
            e.printStackTrace();
            transport.close();
        }
    }
}