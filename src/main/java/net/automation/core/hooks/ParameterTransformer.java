package net.automation.core.hooks;

import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;
import io.cucumber.java.ParameterType;
import net.automation.core.config.Config;
import net.automation.utils.TypeHelper;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParameterTransformer {
    private final Config config = Config.getInstance();

    /**
     * Converts input object into the desired type.
     * The method is used by Cucumber to automatically convert step definition parameters.
     * @param fromValue input object
     * @param toValueType output type
     * @return converted object
     */
    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transformer(Object fromValue, Type toValueType) {
        if (fromValue != null && fromValue instanceof String) {
            fromValue = config.transformVariables(fromValue.toString());
        }

        if (fromValue != null && fromValue instanceof LinkedHashMap<?,?>) {
            Map<String, String> map = (Map<String, String>)fromValue;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }

                String newValue = config.transformVariables(entry.getValue().toString());
                entry.setValue(newValue);
            }
        }

        return TypeHelper.convert(fromValue, toValueType);
    }

    @ParameterType("\"([^\"]*)\"")
    public String str(String value) {
        return config.transformVariables(value);
    }
}