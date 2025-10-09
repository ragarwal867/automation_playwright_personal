package net.automation.clients.api;

import io.restassured.internal.multipart.MultiPartSpecificationImpl;
import net.automation.utils.TypeHelper;

import java.util.HashMap;

public class RestAssuredHelper {
    public static MultiPartSpecificationImpl createJsonMultiPart(String name, String fileName, Object content) {
        MultiPartSpecificationImpl multiPart = new MultiPartSpecificationImpl();
        multiPart.setControlName(name);
        multiPart.setFileName(fileName);
        multiPart.setMimeType("application/json");
        multiPart.setContent(TypeHelper.convertToJson(content));
        multiPart.setHeaders(new HashMap<>());
        multiPart.setControlNameSpecifiedExplicitly(true);
        multiPart.setFileNameSpecifiedExplicitly(true);
        return multiPart;
    }
}
