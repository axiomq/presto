package com.facebook.presto.features.tim.annotations;

import com.google.inject.Binder;
import com.google.inject.Key;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Utility methods for use with @FeatureToggle.
 */
public class FeatureToggles
{

    private FeatureToggles() {}

    /**
     * Creates a {@link FeatureToggle} annotation with {@code name} as the value.
     */
    public static FeatureToggle named(String value)
    {
        return new FeatureToggleImpl(value);
    }

    /**
     * Creates a constant binding to {@code @FeatureToggle(name)} for each entry in {@code properties}.
     */
    public static void bindProperties(Binder binder, Map<String, String> properties)
    {
        binder = binder.skipSources(FeatureToggles.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            binder.bind(Key.get(String.class, new FeatureToggleImpl(key))).toInstance(value);
        }
    }

    /**
     * Creates a constant binding to {@code @FeatureToggle(name)} for each property. This method binds all
     * properties including those inherited from {@link Properties#defaults defaults}.
     */
    public static void bindProperties(Binder binder, Properties properties)
    {
        binder = binder.skipSources(FeatureToggles.class);

        // use enumeration to include the default properties
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String propertyName = (String) e.nextElement();
            String value = properties.getProperty(propertyName);
            binder.bind(Key.get(String.class, new FeatureToggleImpl(propertyName))).toInstance(value);
        }
    }
}
