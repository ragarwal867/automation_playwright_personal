package net.automation.clients.mail;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import net.automation.utils.ExceptionHelper;
import net.automation.utils.ResourcesHelper;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.automation.utils.StreamHelper.toByteArray;
import static org.assertj.core.api.Fail.fail;

public class EmailClient {
    public static final String CONTENT_TYPE_MULTIPART = "multipart/*";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_HTML = "text/html";
    private EmailSettings emailSettings;

    public EmailClient(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    public List<Email> receiveEmails(String expectedPartialSubject) {
        List<Email> receivedEmails = new ArrayList<>();

        try {
            Store store = createStore(emailSettings.getPop3());
            Folder inboxFolder = store.getFolder("INBOX");
            inboxFolder.open(Folder.READ_ONLY);
            receivedEmails = Arrays.stream(inboxFolder.getMessages())
                    .filter(m -> hasMessagePartialSubject(m, expectedPartialSubject))
                    .map(m -> createEmail(m))
                    .toList();
            inboxFolder.close(false);
            store.close();
        } catch (Throwable t) {
            fail("Cannot receive emails. Details: " + ExceptionHelper.getDetailedExceptionInfo(t));
        }

        return receivedEmails;
    }

    public void sendEmail(Email email) {
        try {
            Session session = createSession(emailSettings.getSmtp());
            MimeBodyPart contentBodyPart = createMimeBodyPart(email);
            MimeMultipart signedMultipart = createSignedMultipart(contentBodyPart);
            Message message = createMessage(email, signedMultipart, session);
            Transport.send(message);
        } catch (Throwable t) {
            fail("Cannot send email. Details: " + ExceptionHelper.getDetailedExceptionInfo(t));
        }
    }

    public void deleteEmails(String expectedPartialSubject) {
        try {
            Store store = createStore(emailSettings.getPop3());
            Folder inboxFolder = store.getFolder("INBOX");
            inboxFolder.open(Folder.READ_WRITE);

            Message[] messages = inboxFolder.getMessages();
            for (Message message : messages) {
                if (hasMessagePartialSubject(message, expectedPartialSubject)) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
            }

            inboxFolder.close(true);
            store.close();
        } catch (Throwable t) {
            fail("Cannot delete emails. Details: " + ExceptionHelper.getDetailedExceptionInfo(t));
        }
    }

    private Store createStore(EmailServerSettings emailServerSettings) throws Exception {
        Session session = Session.getInstance(emailServerSettings.getProperties());
        Store store = session.getStore("pop3");
        store.connect(emailServerSettings.getHost(), emailServerSettings.getUsername(), emailServerSettings.getPassword());
        return store;
    }

    private Email createEmail(Message message) {
        Email email = new Email();

        try {
            email.setSender(message.getFrom()[0].toString());
            email.setRecipient(message.getAllRecipients()[0].toString());
            email.setSubject(message.getSubject());
            handleContent(message.getContentType(), message.getContent(), email);
        } catch (Throwable t) {
            fail("Cannot create email. Details: " + ExceptionHelper.getDetailedExceptionInfo(t));
        }

        return email;
    }

    private void handleMultipart(final Multipart multipart, final Email email) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            handleContent(bodyPart.getContentType(), bodyPart, email);
        }
    }

    private void handleContent(final String contentType, final Object content, final Email email) throws Exception {
        String mainType = contentType != null ? contentType.split(";")[0].trim().toLowerCase() : "";
        switch (mainType) {
            case CONTENT_TYPE_MULTIPART -> handleMultipart((Multipart) content, email);
            case CONTENT_TYPE_TEXT -> email.getPlainTextParts().add((String) content);
            case CONTENT_TYPE_HTML -> email.getHtmlTextParts().add((String) content);
            default -> {
                if (content instanceof BodyPart bodyPart) {
                    try (InputStream inputStream = bodyPart.getInputStream()) {
                        final EmailPart emailPart = new EmailPart()
                                .setFilename(bodyPart.getFileName())
                                .setContentType(bodyPart.getContentType())
                                .setContent(toByteArray(inputStream));
                        email.getEmailParts().add(emailPart);
                    }
                }
            }
        }
    }

    private boolean hasMessagePartialSubject(Message message, String expectedPartialSubject) {
        String actualSubject;
        try {
            actualSubject = message.getSubject();
        } catch (MessagingException e) {
            actualSubject = null;
        }

        return actualSubject != null
                ? actualSubject.contains(expectedPartialSubject)
                : false;
    }

    private Session createSession(EmailServerSettings emailServerSettings) {
        return jakarta.mail.Session.getInstance(emailServerSettings.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailServerSettings.getUsername(), emailServerSettings.getPassword());
            }
        });
    }

    private MimeBodyPart createMimeBodyPart(Email email) throws MessagingException {
        MimeMultipart mimeMultipart = new MimeMultipart();
        for (EmailPart emailPart : email.getEmailParts()) {
            MimeBodyPart bodyPart = new MimeBodyPart();
            if (emailPart.getFilename() != null) {
                bodyPart.setFileName(emailPart.getFilename());
            }

            bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(emailPart.getContent(), emailPart.getContentType())));
            bodyPart.setDisposition(Part.ATTACHMENT);
            bodyPart.setHeader("Content-Transfer-Encoding", emailPart.getContentTransferEncoding());
            if (emailPart.getContentEncoding() != null) {
                bodyPart.setHeader("Content-Encoding", emailPart.getContentEncoding());
            }

            mimeMultipart.addBodyPart(bodyPart);
        }

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(mimeMultipart);
        return mimeBodyPart;
    }

    private MimeMultipart createSignedMultipart(MimeBodyPart contentBodyPart) throws Exception {
        KeyStore keyStore = createKeyStoreWithCertificate();

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, emailSettings.getCertificatePassword().toCharArray());
        Certificate certificate = keyStore.getCertificate(alias);

        SMIMESignedGenerator signedGenerator = new SMIMESignedGenerator();
        signedGenerator.addSignerInfoGenerator(createSignerInfoGenerator(privateKey, certificate));
        signedGenerator.addCertificates(new JcaCertStore(Collections.singletonList(certificate)));

        return signedGenerator.generate(contentBodyPart);
    }

    private Message createMessage(Email email, MimeMultipart multipart, Session session) throws Exception {
        return new MimeMessage(session) {{
            setFrom(new InternetAddress(email.getSender()));
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipient()));
            if (email.getSubject() != null) {
                setSubject(email.getSubject());
            }
            setContent(multipart);
        }};
    }

    private KeyStore createKeyStoreWithCertificate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = ResourcesHelper.readResourceAsInputStream(emailSettings.getCertificatePath())) {
            keyStore.load(inputStream, emailSettings.getCertificatePassword().toCharArray());
        }

        return keyStore;
    }

    private SignerInfoGenerator createSignerInfoGenerator(
            PrivateKey privateKey,
            Certificate certificate) throws Exception {
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        return new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().build())
                .build(contentSigner, (X509Certificate) certificate);
    }
}