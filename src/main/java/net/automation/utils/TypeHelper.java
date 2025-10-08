package net.automation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class TypeHelper {
    public static ObjectMapper objectMapper;

    public TypeHelper() {
    }

    public static Object convert(Object input, Type outputType) {
        return objectMapper.convertValue(input, objectMapper.constructType(outputType));
    }

    public static <T> T convert(Object input, Class<T> outputType) {
        return (T)objectMapper.convertValue(input, objectMapper.constructType(outputType));
    }

    public static <T> T convertFromJson(String jsonString, Class<T> outputType) {
        T output = null;

        try {
            output = (T)objectMapper.readValue(jsonString, outputType);
        } catch (JsonProcessingException e) {
        }

        return output;
    }

    public static String convertToJson(Object value) {
        String output = null;

        try {
            output = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
        }

        return output;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        TypeHelper.objectMapper = objectMapper;
    }

    static {
        objectMapper = (new ObjectMapper()).enable(new MapperFeature[]{MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS}).enable(new MapperFeature[]{MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES}).enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING).registerModule(new JavaTimeModule()).setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }
}
