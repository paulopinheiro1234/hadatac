package org.hadatac.console.providers;

import akka.actor.Cancellable;
import com.feth.play.module.mail.Mailer;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;

import javax.inject.Inject;


public class MyService {
    @Inject
     MailerClient mailerClient;

    protected  String sendMail(final String subject, final Mailer.Mail.Body body,
                                          final String recipient) {
        Email email = new Email()
                      .setSubject(subject)
                      .setBodyText(body.getText())
                       .setBodyHtml(body.getHtml())
                       .setFrom("user21email@gmail.com") //TODO : update this
                       .addTo(recipient);
        return sendMail(email);//new Mailer.Mail(subject, body, recipient));
    }

    protected String sendMail(final Email mail) {
        return mailerClient.send(mail);
    }


}
