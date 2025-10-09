package net.automation.clients.mail;

import lombok.Data;
import lombok.experimental.Accessors;
import net.automation.core.config.Config;

@Accessors(chain = true)
@Data
public class EmailSettings {
    private String certificatePath;
    private String certificatePassword;
    private String sender;
    private String recipient;
    private EmailServerSettings smtp;
    private EmailServerSettings pop3;

    public static EmailSettings fromConfig(Config config) {
        return new EmailSettings()
                .setCertificatePath(config.getProperty("mail.certificate.path"))
                .setCertificatePassword(config.getProperty("mail.certificate.password"))
                .setSender(config.getProperty("mail.sender"))
                .setRecipient(config.getProperty("mail.recipient"))
                .setSmtp(new EmailServerSettings("smtp")
                        .setHost(config.getProperty("mail.smtp.host"))
                        .setPort(Integer.parseInt(config.getProperty("mail.smtp.port")))
                        .setUsername(config.getProperty("mail.smtp.username"))
                        .setPassword(config.getProperty("mail.smtp.password")))
                .setPop3(new EmailServerSettings("pop3")
                        .setHost(config.getProperty("mail.pop3.host"))
                        .setPort(Integer.parseInt(config.getProperty("mail.pop3.port")))
                        .setUsername(config.getProperty("mail.pop3.username"))
                        .setPassword(config.getProperty("mail.pop3.password")));
    }
}
