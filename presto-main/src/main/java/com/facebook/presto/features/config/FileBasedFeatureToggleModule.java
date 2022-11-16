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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.strategy.AllowAllStrategy;
import com.facebook.presto.features.strategy.AllowListToggleStrategy;
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

import static com.facebook.presto.features.config.ConfigurationParser.parseConfiguration;
import static com.facebook.presto.features.strategy.FeatureToggleStrategyFactory.ALLOW_ALL;
import static com.facebook.presto.features.strategy.FeatureToggleStrategyFactory.ALLOW_LIST;
import static com.facebook.presto.features.strategy.FeatureToggleStrategyFactory.OS_TOGGLE;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileBasedFeatureToggleModule
        implements Module
{
    private static final Logger log = Logger.get(FileBasedFeatureToggleModule.class);

    @Override
    public void configure(Binder binder)
    {
        MapBinder<String, FeatureToggleStrategy> featureToggleStrategyMap = MapBinder.newMapBinder(binder, String.class, FeatureToggleStrategy.class);
        featureToggleStrategyMap.addBinding(ALLOW_ALL).to(AllowAllStrategy.class);
        featureToggleStrategyMap.addBinding(ALLOW_LIST).to(AllowListToggleStrategy.class);
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
