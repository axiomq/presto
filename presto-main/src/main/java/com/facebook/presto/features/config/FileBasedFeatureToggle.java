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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.facebook.presto.features.config.ConfigurationParser.parseConfiguration;

public class FileBasedFeatureToggle
        implements FeatureToggle
{
    private final Map<String, FeatureConfiguration> featureConfigurationMap;
    private final Map<String, FeatureConfiguration> featureIdConfigurationMap;
    private final FeatureToggleStrategyFactory featureToggleStrategyFactory;

    public FileBasedFeatureToggle(FeatureToggleConfig config, FeatureToggleStrategyFactory featureToggleStrategyFactory)
    {
        this.featureIdConfigurationMap = parseConfiguration(config);
        this.featureConfigurationMap = featureIdConfigurationMap.values().stream()
                .collect(Collectors.toMap(configuration -> configuration.getFeatureClass() != null ? configuration.getFeatureClass() : configuration.getFeatureId(), Function.identity()));
        this.featureToggleStrategyFactory = featureToggleStrategyFactory;
    }

    @Override
    public boolean check(String featureId)
    {
        FeatureConfiguration configuration = featureIdConfigurationMap.get(featureId);
        AtomicBoolean enabled = new AtomicBoolean(true);
        if (configuration != null) {
            enabled.set(configuration.isEnabled());
            if (configuration.getFeatureToggleStrategyConfig() != null) {
                String toggleStrategyClass = configuration.getFeatureToggleStrategyConfig().getToggleStrategyClass();
                if (toggleStrategyClass != null) {
                    Optional<FeatureToggleStrategy> strategy = Optional.ofNullable(featureToggleStrategyFactory.get(toggleStrategyClass));
                    return strategy
                            .map(featureToggleStrategy -> enabled.get() && featureToggleStrategy.check(this, featureId))
                            .orElse(enabled.get());
                }
            }
            return configuration.isEnabled();
        }
        return enabled.get();
    }

    @Override
    public String getCurrentInstance(String featureClassName)
    {
        return featureConfigurationMap.get(featureClassName).getCurrentInstance();
    }

    @Override
    public String getDefaultInstance(String featureClassName)
    {
        return featureConfigurationMap.get(featureClassName).getDefaultInstance();
    }

    @Override
    public boolean check(String featureId, Object object)
    {
        FeatureConfiguration configuration = featureIdConfigurationMap.get(featureId);
        AtomicBoolean enabled = new AtomicBoolean(true);
        if (configuration != null) {
            enabled.set(configuration.isEnabled());
            if (configuration.getFeatureToggleStrategyConfig() != null) {
                String toggleStrategyClass = configuration.getFeatureToggleStrategyConfig().getToggleStrategyClass();
                if (toggleStrategyClass != null) {
                    Optional<FeatureToggleStrategy> strategy = Optional.ofNullable(featureToggleStrategyFactory.get(toggleStrategyClass));
                    return strategy
                            .map(featureToggleStrategy -> enabled.get() && featureToggleStrategy.check(this, featureId, object))
                            .orElse(enabled.get());
                }
            }
        }
        return enabled.get();
    }

    @Override
    public FeatureConfiguration getFeatureConfiguration(String featureId)
    {
        return featureIdConfigurationMap.get(featureId);
    }
}
