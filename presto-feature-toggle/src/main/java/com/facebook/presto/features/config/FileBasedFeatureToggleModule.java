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

import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.http.FeatureToggleInfo;
import com.facebook.presto.features.plugin.storage.FeatureToggleConfigurationManager;
import com.facebook.presto.features.strategy.FeatureToggleStrategyFactory;
import com.facebook.presto.spi.features.FeatureToggleConfiguration;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;

import static com.facebook.airlift.jaxrs.JaxrsBinder.jaxrsBinder;
import static com.facebook.presto.features.config.ConfigurationParser.parseConfiguration;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileBasedFeatureToggleModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        jaxrsBinder(binder).bind(FeatureToggleInfo.class);
        binder.bind(FeatureToggleStrategyFactory.class);
        binder.bind(PrestoFeatureToggle.class).in(Scopes.SINGLETON);
        binder.bind(FeatureToggleConfigurationManager.class).in(Scopes.SINGLETON);
    }

    @Inject
    @Provides
    public FeatureToggleConfiguration getFeaturesConfiguration(FeatureToggleConfig config, FeatureToggleConfigurationManager configurationManager)
    {
        if (config.getRefreshPeriod() != null) {
            return ForwardingFeaturesConfiguration.of(memoizeWithExpiration(
                    () -> configurationManager.getConfigurationSource(config.getConfigType()).getConfiguration(),
                    config.getRefreshPeriod().toMillis(),
                    MILLISECONDS));
        }
        return new FileBasedFeatureToggleConfiguration(parseConfiguration(config));
    }
}
