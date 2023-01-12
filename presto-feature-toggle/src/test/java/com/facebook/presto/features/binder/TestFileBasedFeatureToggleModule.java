/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.features.binder;

import com.facebook.presto.features.config.FeatureToggleConfig;
import com.facebook.presto.features.config.FeatureToggleConfiguration;
import com.facebook.presto.features.config.FileBasedFeatureToggleConfiguration;
import com.facebook.presto.features.config.ForwardingFeaturesConfiguration;
import com.facebook.presto.features.strategy.FeatureToggleStrategy;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import static com.facebook.presto.features.binder.TestConfigurationParser.parseConfiguration;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * TestFeatureToggleModule configures base Feature Toggle strategies and configures providers for FeatureToggleStrategyFactory and FeatureToggleConfiguration
 */
public class TestFileBasedFeatureToggleModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        MapBinder.newMapBinder(binder, String.class, FeatureToggleStrategy.class);
        binder.bind(PrestoFeatureToggle.class).in(Singleton.class);
    }

    @Inject
    @Provides
    public FeatureToggleConfiguration getFeaturesConfiguration(FeatureToggleConfig config)
    {
        if (config.getRefreshPeriod() != null) {
            return ForwardingFeaturesConfiguration.of(memoizeWithExpiration(
                    () -> new FileBasedFeatureToggleConfiguration(parseConfiguration(config)),
                    config.getRefreshPeriod().toMillis(),
                    MILLISECONDS));
        }
        return new FileBasedFeatureToggleConfiguration(parseConfiguration(config));
    }
}
