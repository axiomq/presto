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

import com.facebook.airlift.configuration.AbstractConfigurationAwareModule;
import com.facebook.airlift.log.Logger;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;

import java.util.Map;

import static com.facebook.presto.features.config.Util.classForName;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileBasedFeatureToggleModule
        extends AbstractConfigurationAwareModule
{
    private static final Logger log = Logger.get(FileBasedFeatureToggleModule.class);

    @Override
    protected void setup(Binder binder)
    {
        FeatureToggleConfig config = buildConfigObject(FeatureToggleConfig.class);
        Map<String, FeatureConfiguration> map = ConfigurationParser.parseConfiguration(config);
        MapBinder<String, Object> featureMapBinder = MapBinder.newMapBinder(binder, String.class, Object.class);
        map.forEach((featureClassName, configuration) ->
                configuration.getFeatureInstances().forEach(instanceClassName ->
                        featureMapBinder.addBinding(instanceClassName).to(classForName(instanceClassName))));
    }

    @Inject
    @Provides
    public FeatureToggle getFeatureToggle(FeatureToggleConfig config)
    {
        if (config.getRefreshPeriod() != null) {
            return ForwardingFeatureToggle.of(memoizeWithExpiration(
                    () -> {
                        log.info("Refreshing feature toggle control from %s", config.getConfigSource());
                        return new FileBasedFeatureToggle(config);
                    },
                    config.getRefreshPeriod().toMillis(),
                    MILLISECONDS));
        }
        return new FileBasedFeatureToggle(config);
    }
}
