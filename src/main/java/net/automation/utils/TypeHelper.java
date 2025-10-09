package net.automation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.reflections.ReflectionUtils;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.junit.Assert.fail;

public class TypeHelper {
    public static ObjectMapper objectMapper;

    public TypeHelper() {
    }

    /**
     * Create new instance of the given class
     *
     * @param clazz type of class
     * @param <T>   type of instance we want to create
     * @return new instance of the class
     */
    public static <T> T createInstance(Class<T> clazz) {
        T instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail("Cannot create new instance of %s. Details: %s".formatted(
                    clazz.getName(),
                    e.getMessage()));
        }

        return instance;
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

    /**
     * Get all getter methods
     *
     * @param clazz the class on which the methods should be returned
     * @return List of getter methods
     */
    public static Set<Method> getAllGetters(Class clazz) {
        return ReflectionUtils.getAllMethods(
                clazz,
                ReflectionUtils.withModifier(Modifier.PUBLIC),
                ReflectionUtils.withPrefix("get"),
                ReflectionUtils.withParametersCount(0));
    }

    /**
     * Invoke a method on an object
     *
     * @param method     the method to invoke
     * @param object     the object on which the method should be invoked
     * @param methodArgs optional method arguments
     * @return results of the method
     */
    public static Object invoke(Method method, Object object, Object... methodArgs) {
        Object output = null;

        try {
            output = method.invoke(object, methodArgs);
        } catch (Exception ex) {
            fail("Cannot invoke method '%s'. Details: %s".formatted(
                    method.getName(),
                    ex.getMessage()));
        }

        return output;
    }

}
