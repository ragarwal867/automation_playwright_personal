package net.automation.clients.api;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class CsrfTokenSettings {
    private Boolean extractTokenFromCookies;
    private String cookieName;
    private String headerName;

}
