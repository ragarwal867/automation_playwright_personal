package net.automation.core.context;

import net.automation.utils.models.ContextType;

import java.util.HashMap;
import java.util.Map;

public class ContextRegistry {
    public static final Map<ContextType, ScenarioContext> contextMap = new HashMap<>();

    public static <T> void put(ContextType key, T value) {
        contextMap.put(key, (ScenarioContext) value);
    }

    public static <T> T get(ContextType key, Class<T> clazz) {
        return clazz.cast(contextMap.get(key));
    }

    public static void clear() {
        contextMap.clear();
    }
}
