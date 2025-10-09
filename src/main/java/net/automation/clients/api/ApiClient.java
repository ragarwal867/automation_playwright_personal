package net.automation.clients.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.automation.utils.Logger;
import net.automation.utils.TypeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.restassured.RestAssured.config;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.junit.Assert.assertEquals;

@Accessors(chain = true)
public class ApiClient {
    private static RestAssuredConfig config = config()
            .objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> TypeHelper.objectMapper));

    @Getter
    @Setter
    private String baseUrl;

    @Getter
    @Setter
    private Map<String, String> defaultHeaders;

    @Getter
    @Setter
    private Map<String, String> defaultCookies;

    @Getter
    @Setter
    private CsrfTokenSettings csrfTokenSettings;

    public ApiClient() {
        defaultHeaders = new HashMap<>();
        defaultCookies = new HashMap<>();
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        defaultHeaders = new HashMap<>();
        defaultCookies = new HashMap<>();
    }

    public Response invoke(
            Method method,
            String resourceUrl,
            int expectedStatusCode) {
        return invoke(method, resourceUrl, expectedStatusCode, r -> {
        });
    }

    public Response invoke(
            Method method,
            String resourceUrl,
            int expectedStatusCode,
            Consumer<RequestSpecBuilder> updateRequest) {

        RequestSpecBuilder requestSpecBuilder = createRequestSpecBuilder(ContentType.JSON);

        updateRequest.accept(requestSpecBuilder);
        RequestSpecification requestSpecification = requestSpecBuilder.build();
        Response response = RestAssured.given(requestSpecification).request(method, resourceUrl);

        Logger.logDebug("API %s %s (%s) (%s ms)".formatted(method, resourceUrl, response.getStatusCode(), response.getTimeIn(TimeUnit.MILLISECONDS)));
        assertEquals(expectedStatusCode, response.getStatusCode());
        processResponse(response);

        return response;
    }

    public Response invoke(
            Method method,
            String resourceUrl,
            int expectedStatusCode,
            Consumer<RequestSpecBuilder> updateRequest,
            String clientId,
            String clientSecret,
            ContentType contentType) {

        RequestSpecBuilder requestSpecBuilder = createRequestSpecBuilder(contentType);

        updateRequest.accept(requestSpecBuilder);
        RequestSpecification requestSpecification = requestSpecBuilder.build();

        Response response = RestAssured.given(requestSpecification)
                .auth().preemptive().basic(clientId, clientSecret)
                .request(method, resourceUrl);

        Logger.logDebug("API %s %s (%s) (%s ms)".formatted(method, resourceUrl, response.getStatusCode(), response.getTimeIn(TimeUnit.MILLISECONDS)));
        assertEquals(expectedStatusCode, response.getStatusCode());
        processResponse(response);

        return response;
    }

    private RequestSpecBuilder createRequestSpecBuilder(ContentType contentType) {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.setConfig(config);
        requestSpecBuilder.addCookies(getDefaultCookies());
        requestSpecBuilder.addHeaders(getDefaultHeaders());
        requestSpecBuilder.setBaseUri(getBaseUrl());
        requestSpecBuilder.setContentType(contentType);
        return requestSpecBuilder;
    }

    public void processResponse(Response response) {
        getDefaultCookies().putAll(response.cookies());

        if (csrfTokenSettings != null && csrfTokenSettings.getExtractTokenFromCookies()) {
            String xsrfToken = response.cookies().get(csrfTokenSettings.getCookieName());
            if (xsrfToken != null) {
                getDefaultHeaders().put(csrfTokenSettings.getHeaderName(), xsrfToken);
            }
        }
    }
}
