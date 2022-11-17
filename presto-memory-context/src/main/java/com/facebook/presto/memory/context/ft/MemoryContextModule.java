package com.facebook.presto.memory.context.ft;

import com.google.inject.Binder;
import com.google.inject.Module;

import static com.facebook.presto.features.binder.FeatureToggleBinder.featureToggleBinder;

public class MemoryContextModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        featureToggleBinder(binder, MemoryFeatureToggleInterface.class)
                .defaultClass(MemoryFeatureToggleInterfaceImpl.class)
                .featureId("memory-feature").bind();
    }
}
