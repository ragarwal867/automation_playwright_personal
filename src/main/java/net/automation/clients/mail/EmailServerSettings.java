package net.automation.clients.mail;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Properties;

@Accessors(chain = true)
@Data
public class EmailServerSettings {
    private String host;
    private int port;
    private String username;
    private String password;
    private String serverType;

    public EmailServerSettings(String serverType) {
        this.serverType = serverType;
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.%s.host".formatted(serverType), host);
        properties.put("mail.%s.port".formatted(serverType), port);
        properties.put("mail.%s.auth".formatted(serverType), "true");
        properties.put("mail.mime.encodeeol.strict", "true");
        properties.put("mail.debug", "false");
        return properties;
    }
}
