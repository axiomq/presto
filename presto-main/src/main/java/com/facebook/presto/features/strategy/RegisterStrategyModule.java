package com.facebook.presto.features.strategy;

import com.google.inject.Binder;
import com.google.inject.Module;

import static com.facebook.presto.features.binder.FeatureToggleBinder.featureToggleBinder;
import static com.facebook.presto.features.strategy.FeatureToggleStrategyFactory.ALLOW_LIST;

public class RegisterStrategyModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        featureToggleBinder(binder).registerToggleStrategy(ALLOW_LIST, AllowListToggleStrategy.class);
    }
}
