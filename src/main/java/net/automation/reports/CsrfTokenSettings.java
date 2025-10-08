package net.automation.reports;

public class CsrfTokenSettings {
    private Boolean extractTokenFromCookies;
    private String cookieName;
    private String headerName;

    public CsrfTokenSettings() {
    }

    public Boolean getExtractTokenFromCookies() {
        return this.extractTokenFromCookies;
    }

    public String getCookieName() {
        return this.cookieName;
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public CsrfTokenSettings setExtractTokenFromCookies(Boolean extractTokenFromCookies) {
        this.extractTokenFromCookies = extractTokenFromCookies;
        return this;
    }

    public CsrfTokenSettings setCookieName(String cookieName) {
        this.cookieName = cookieName;
        return this;
    }

    public CsrfTokenSettings setHeaderName(String headerName) {
        this.headerName = headerName;
        return this;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CsrfTokenSettings)) {
            return false;
        } else {
            CsrfTokenSettings other = (CsrfTokenSettings)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$extractTokenFromCookies = this.getExtractTokenFromCookies();
                Object other$extractTokenFromCookies = other.getExtractTokenFromCookies();
                if (this$extractTokenFromCookies == null) {
                    if (other$extractTokenFromCookies != null) {
                        return false;
                    }
                } else if (!this$extractTokenFromCookies.equals(other$extractTokenFromCookies)) {
                    return false;
                }

                Object this$cookieName = this.getCookieName();
                Object other$cookieName = other.getCookieName();
                if (this$cookieName == null) {
                    if (other$cookieName != null) {
                        return false;
                    }
                } else if (!this$cookieName.equals(other$cookieName)) {
                    return false;
                }

                Object this$headerName = this.getHeaderName();
                Object other$headerName = other.getHeaderName();
                if (this$headerName == null) {
                    if (other$headerName != null) {
                        return false;
                    }
                } else if (!this$headerName.equals(other$headerName)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof CsrfTokenSettings;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $extractTokenFromCookies = this.getExtractTokenFromCookies();
        result = result * 59 + ($extractTokenFromCookies == null ? 43 : $extractTokenFromCookies.hashCode());
        Object $cookieName = this.getCookieName();
        result = result * 59 + ($cookieName == null ? 43 : $cookieName.hashCode());
        Object $headerName = this.getHeaderName();
        result = result * 59 + ($headerName == null ? 43 : $headerName.hashCode());
        return result;
    }

    public String toString() {
        Boolean var10000 = this.getExtractTokenFromCookies();
        return "CsrfTokenSettings(extractTokenFromCookies=" + var10000 + ", cookieName=" + this.getCookieName() + ", headerName=" + this.getHeaderName() + ")";
    }
}