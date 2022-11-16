package com.facebook.presto.features.config;

import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.strategy.AllowAllStrategy;
import com.facebook.presto.features.strategy.FeatureToggleStrategy;
import com.facebook.presto.features.strategy.FeatureToggleStrategyFactory;
import com.facebook.presto.features.strategy.OsToggleStrategy;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import java.util.Map;

import static com.facebook.presto.features.strategy.FeatureToggleStrategyFactory.ALLOW_ALL;
import static com.facebook.presto.features.strategy.FeatureToggleStrategyFactory.OS_TOGGLE;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TestFeatureToggleModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        MapBinder<String, FeatureToggleStrategy> featureToggleStrategyMap = MapBinder.newMapBinder(binder, String.class, FeatureToggleStrategy.class);
        featureToggleStrategyMap.addBinding(ALLOW_ALL).to(AllowAllStrategy.class);
        featureToggleStrategyMap.addBinding(OS_TOGGLE).to(OsToggleStrategy.class);
        binder.bind(PrestoFeatureToggle.class).in(Singleton.class);
    }

    @Inject
    @Provides
    public FeatureToggleStrategyFactory getFeatureToggleStrategyFactory(Map<String, FeatureToggleStrategy> featureToggleStrategyMap)
    {
        return new FeatureToggleStrategyFactory(featureToggleStrategyMap);
    }

    @Inject
    @Provides
    public FeatureToggleConfiguration getFeaturesConfiguration(Map<String, FeatureConfiguration> config)
    {
        return ForwardingFeaturesConfiguration.of(memoizeWithExpiration(
                () -> new FileBasedFeatureToggleConfiguration(config),
                5000L,
                MILLISECONDS));
    }
}
