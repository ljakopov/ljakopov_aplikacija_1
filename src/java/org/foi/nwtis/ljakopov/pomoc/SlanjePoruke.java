/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.pomoc;

import java.util.Date;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author ljakopov
 */
public class SlanjePoruke {
    
    
    public static String saljiPoruku(String posluzitelj, String salje, String prima, String predmet, String sadrzaj) {
        //TODO dodaj za slanje poruke prema primjeru s predavanja koji je priložen uz zadaću

        String status;

        try {
            // Create the JavaMail session
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);

            Session session
                    = Session.getInstance(properties, null);

            // Construct the message
            Message message = new MimeMessage(session);

            // Set the from address
            Address fromAddress = new InternetAddress(salje);
            message.setFrom(fromAddress);

            // Parse and set the recipient addresses
            Address[] toAddresses = InternetAddress.parse(prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);

            // Set the subject and text
            message.setSentDate(new Date());
            message.setSubject(predmet);
            message.setText(sadrzaj);

            Transport.send(message);

            status = "Your message was sent.";
        } catch (AddressException e) {
            status = "There was an error parsing the addresses.";
        } catch (SendFailedException e) {
            status = "There was an error sending the message.";
        } catch (MessagingException e) {
            status = "There was an unexpected error.";
        }

        return "PoslanaPoruka";
    }
    
}
