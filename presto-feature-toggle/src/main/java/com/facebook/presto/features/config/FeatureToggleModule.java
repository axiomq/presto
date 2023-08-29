package com.facebook.presto.features.config;

import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.http.FeatureToggleInfo;
import com.facebook.presto.features.strategy.FeatureToggleStrategyFactory;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import static com.facebook.airlift.jaxrs.JaxrsBinder.jaxrsBinder;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FeatureToggleModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        jaxrsBinder(binder).bind(FeatureToggleInfo.class);
        binder.bind(FeatureToggleStrategyFactory.class);
        binder.bind(PrestoFeatureToggle.class).in(Singleton.class);
    }

    @Inject
    @Provides
    public FeatureToggleConfiguration getFeaturesConfiguration(FeatureToggleConfig config, FeatureToggleConfigurationSource source)
    {
        if (config.getRefreshPeriod() != null) {
            return ForwardingFeaturesConfiguration.of(memoizeWithExpiration(
                    () -> new FileBasedFeatureToggleConfiguration(source.parseConfiguration(config)),
                    config.getRefreshPeriod().toMillis(),
                    MILLISECONDS));
        }
        return new FileBasedFeatureToggleConfiguration(source.parseConfiguration(config));
    }
}
