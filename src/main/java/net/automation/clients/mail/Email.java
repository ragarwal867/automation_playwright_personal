package net.automation.clients.mail;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
public class Email {
    private String sender;
    private String recipient;
    private String subject;
    private List<String> plainTextParts = new ArrayList<>();
    private List<String> htmlTextParts = new ArrayList<>();
    private List<EmailPart> emailParts = new ArrayList<>();
}
