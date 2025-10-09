package net.automation.core.config;

import lombok.Getter;
import net.automation.utils.ResourcesHelper;
import net.automation.utils.StringHelper;
import net.automation.utils.lazyloader.LazyLoader;
import net.automation.utils.lazyloader.OneTimeLazyLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Fail.fail;

public class Config {
    @Getter
    private static Config instance = new Config();

    @Getter
    private final Map<String, LazyLoader<String>> properties = new HashMap<>();

    @Getter
    private String configPath;

    private Config() {
//        try {
//            // Properties
//            Properties configProperties = new Properties();
//            configPath = System.getProperty("configFileName");
//
//            if (configPath == null) {
//                Properties defaultProperties = new Properties();
//                defaultProperties.load(ResourcesHelper.getStream("config/config_default.properties"));
//                configPath = defaultProperties.get("configFileName").toString();
//            }
//
//            InputStream configInputStream = ResourcesHelper.getStream(configPath);
//            configProperties.load(configInputStream);
//            configProperties.putAll(System.getProperties());
//
//            // Variables
//            for (Map.Entry<Object, Object> entry : configProperties.entrySet()) {
//                properties.put(entry.getKey().toString(), new OneTimeLazyLoader<>(() -> entry.getValue().toString()));
//            }
//        } catch (Exception e) {
//            fail("Cannot read test configuration. Details: %s".formatted(e.getMessage()));
//        }
    }

    public String transformVariables(String text) {
        return StringHelper.transformVariables(text, properties);
    }

    public Properties getHibernatePropertiesFor(String databaseName) {
        Properties output = new Properties();
        for (Map.Entry<String, LazyLoader<String>> property : properties.entrySet()) {
            String key = property.getKey().toString();
            if (key.startsWith(databaseName + ".hibernate")) {
                output.setProperty(key.substring(databaseName.length() + 1), property.getValue().get());
            }
        }

        return output;
    }

    public boolean hasProperty(String key) {
        if (key == null) {
            return false;
        }

        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        if (key == null) {
            return null;
        }

        if (!properties.containsKey(key)) {
            fail("Cannot find property: " + key);
        }

        return properties.get(key).get();
    }

    public String getUrl() {
        return this.getProperty("url");
    }
}
