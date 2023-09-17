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
package com.facebook.presto.features.config;

import com.facebook.presto.features.annotations.FeatureToggles;
import com.facebook.presto.features.binder.Feature;
import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.http.FeatureToggleInfo;
import com.facebook.presto.features.strategy.FeatureToggleStrategy;
import com.facebook.presto.features.strategy.FeatureToggleStrategyFactory;
import com.facebook.presto.spi.features.FeatureToggleConfiguration;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import io.airlift.units.Duration;

import static com.facebook.airlift.jaxrs.JaxrsBinder.jaxrsBinder;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FeatureToggleModule
        implements Module
{
    public static final String FEATURE_INSTANCE_MAP = "feature-instance-map";
    public static final String FEATURE_MAP = "feature-map";
    public static final String FEATURE_STRATEGY_MAP = "feature-strategy-map";
    private static final String DEFAULT_DURATION = "60s";

    @Override
    public void configure(Binder binder)
    {
        jaxrsBinder(binder).bind(FeatureToggleInfo.class);
        binder.bind(PrestoFeatureToggle.class).in(Scopes.SINGLETON);
        binder.bind(FeatureToggleConfigurationManager.class).in(Scopes.SINGLETON);
        binder.bind(FeatureToggleStrategyFactory.class);
        newMapBinder(binder, String.class, Object.class, FeatureToggles.named(FEATURE_INSTANCE_MAP));
        newMapBinder(binder, new TypeLiteral<String>() {}, new TypeLiteral<Feature<?>>() {}, FeatureToggles.named(FEATURE_MAP));
        newMapBinder(binder, String.class, FeatureToggleStrategy.class, FeatureToggles.named(FEATURE_STRATEGY_MAP));
    }

    @Inject
    @Provides
    public FeatureToggleConfiguration getFeaturesConfiguration(FeatureToggleConfig config, FeatureToggleConfigurationManager configurationManager)
    {
        String configSourceType;
        if (config.getConfigSourceType() == null) {
            configSourceType = DefaultConfigurationSource.NAME;
        }
        else {
            configSourceType = config.getConfigSourceType();
        }

        Duration refreshPeriod;
        if (config.getRefreshPeriod() == null) {
            refreshPeriod = Duration.valueOf(DEFAULT_DURATION);
        }
        else {
            refreshPeriod = config.getRefreshPeriod();
        }
        return ForwardingFeaturesConfiguration.of(memoizeWithExpiration(
                () -> configurationManager.getConfigurationSource(configSourceType).getConfiguration(),
                refreshPeriod.toMillis(),
                MILLISECONDS));
    }
}
