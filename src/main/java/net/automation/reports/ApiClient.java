package net.automation.reports;

import org.junit.Assert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;;

public class ApiClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_TIMEOUT)
            .build();

    private String baseUrl;
    private Map<String, String> defaultHeaders;
    private Map<String, String> defaultCookies;
    private CsrfTokenSettings csrfTokenSettings;

    public ApiClient() {
        this.defaultHeaders = new HashMap<>();
        this.defaultCookies = new HashMap<>();
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.defaultHeaders = new HashMap<>();
        this.defaultCookies = new HashMap<>();
    }

    public String invoke(net.automation.reports.Method method, String resourceUrl, int expectedStatusCode) {
        return invoke(method, resourceUrl, expectedStatusCode, (r) -> {});
    }

    public String invoke(net.automation.reports.Method method, String resourceUrl, int expectedStatusCode, Consumer<RequestBuilder> updateRequest) {
        RequestBuilder builder = new RequestBuilder(method, resourceUrl)
                .setHeaders(defaultHeaders)
                .setCookies(defaultCookies);

        updateRequest.accept(builder);
        HttpRequest request = builder.build();

        long startTime = System.nanoTime();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);


            Assert.assertEquals(expectedStatusCode, response.statusCode());

            processResponse(response);
            return response.body();
        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Request to " + resourceUrl + " timed out", e);
        } catch (Exception e) {
            throw new RuntimeException("API call failed for " + resourceUrl, e);
        }
    }

    public String invoke(Method method, String resourceUrl, int expectedStatusCode,
                         Consumer<RequestBuilder> updateRequest, String clientId, String clientSecret,
                         ContentType contentType) {
        RequestBuilder builder = new RequestBuilder(method, resourceUrl)
                .setHeaders(defaultHeaders)
                .setCookies(defaultCookies)
                .setAuth(clientId, clientSecret)
                .setContentType(contentType);

        updateRequest.accept(builder);
        HttpRequest request = builder.build();

        long startTime = System.nanoTime();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            Assert.assertEquals(expectedStatusCode, response.statusCode());
            processResponse(response);
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("API call with auth failed for " + resourceUrl, e);
        }
    }

    private void processResponse(HttpResponse<String> response) {
        // Cookies come from 'Set-Cookie' headers
        response.headers().allValues("Set-Cookie").forEach(cookie -> {
            String[] parts = cookie.split(";", 2);
            String[] kv = parts[0].split("=", 2);
            if (kv.length == 2) {
                defaultCookies.put(kv[0].trim(), kv[1].trim());
            }
        });

        // Handle CSRF token extraction
        if (csrfTokenSettings != null && csrfTokenSettings.getExtractTokenFromCookies()) {
            String token = defaultCookies.get(csrfTokenSettings.getCookieName());
            if (token != null) {
                defaultHeaders.put(csrfTokenSettings.getHeaderName(), token);
            }
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ApiClient setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public ApiClient setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public Map<String, String> getDefaultCookies() {
        return defaultCookies;
    }

    public ApiClient setDefaultCookies(Map<String, String> defaultCookies) {
        this.defaultCookies = defaultCookies;
        return this;
    }

    public CsrfTokenSettings getCsrfTokenSettings() {
        return csrfTokenSettings;
    }

    public ApiClient setCsrfTokenSettings(CsrfTokenSettings csrfTokenSettings) {
        this.csrfTokenSettings = csrfTokenSettings;
        return this;
    }

    // ---------- Inner Request Builder Class ----------
    public static class RequestBuilder {
        private final net.automation.reports.Method method;
        private final String url;
        private String body;
        private String auth;
        private ContentType contentType = ContentType.JSON;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();

        public RequestBuilder(net.automation.reports.Method method, String url) {
            this.method = method;
            this.url = url;
        }

        public RequestBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public RequestBuilder setHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public RequestBuilder setCookies(Map<String, String> cookies) {
            this.cookies.putAll(cookies);
            return this;
        }

        public RequestBuilder setContentType(ContentType type) {
            this.contentType = type;
            return this;
        }

        public RequestBuilder setAuth(String clientId, String clientSecret) {
            this.auth = clientId + ":" + clientSecret;
            return this;
        }

        public HttpRequest build() {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(DEFAULT_TIMEOUT);

            // Build cookies
            if (!cookies.isEmpty()) {
                String cookieHeader = cookies.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("");
                builder.header("Cookie", cookieHeader);
            }

            builder.header("Content-Type", contentType.toString());
            headers.forEach(builder::header);

            if (auth != null) {
                String encoded = java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                builder.header("Authorization", "Basic " + encoded);
            }

            switch (method) {
                case POST -> builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                case PUT -> builder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                case DELETE -> builder.DELETE();
                case GET -> builder.GET();
                default -> throw new UnsupportedOperationException("Unsupported method: " + method);
            }

            return builder.build();
        }
    }
}
