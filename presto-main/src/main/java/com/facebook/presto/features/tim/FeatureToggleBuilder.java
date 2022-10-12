package com.facebook.presto.features.tim;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FeatureToggleBuilder<T>
{
    private final Class<T> featureClass;
    private Class<T> defaultImplementation;
    private Set<Class<T>> allImplementations = new HashSet<>();
    private boolean hotReloadable;

    private FeatureToggleBuilder(Class<T> featureClass)
    {
        this.featureClass = featureClass;
    }

    public static <T> FeatureToggleBuilder<T> featureToggle(Class<T> featureClass)
    {
        return new FeatureToggleBuilder<>(featureClass);
    }

    public FeatureToggleBuilder<T> defaultImplementation(Class<T> defaultImplementation)
    {
        this.defaultImplementation = defaultImplementation;
        return this;
    }

    public FeatureToggleBuilder<T> allOf(Class<T> ... implementations)
    {
       this.allImplementations.addAll(Arrays.asList(implementations));
        return this;
    }

    public FeatureToggleBuilder<T> hotReloadable(boolean hotReloadable)
    {
        this.hotReloadable = hotReloadable;
        return this;
    }
}
