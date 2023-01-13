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
package com.facebook.presto.features.http;

import com.facebook.drift.annotations.ThriftField;
import com.facebook.drift.annotations.ThriftStruct;
import com.facebook.presto.features.binder.Feature;
import com.facebook.presto.features.config.FeatureToggleConfiguration;
import com.facebook.presto.features.strategy.FeatureToggleStrategyConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ThriftStruct
public class FeatureStrategyInfo
{
    private boolean active;
    private String strategyName;
    private Map<String, String> config;

    @JsonProperty
    @ThriftField(1)
    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @JsonProperty
    @ThriftField(2)
    public String getStrategyName()
    {
        return strategyName;
    }

    public void setStrategyName(String strategyName)
    {
        this.strategyName = strategyName;
    }

    @JsonProperty
    @ThriftField(3)
    public Map<String, String> getConfig()
    {
        return config;
    }

    public void setConfig(Map<String, String> config)
    {
        this.config = config;
    }

    public static FeatureStrategyInfo strategy(Feature<?> feature, FeatureToggleConfiguration configuration)
    {
        Optional<FeatureToggleStrategyConfig> strategyConfigOptional = feature.getConfiguration().getFeatureToggleStrategyConfig();
        if (strategyConfigOptional.isPresent()) {
            FeatureToggleStrategyConfig strategyConfig = strategyConfigOptional.get();
            FeatureStrategyInfo strategyInfo = new FeatureStrategyInfo();
            boolean active = strategyConfig.active();
            strategyInfo.setActive(active);
            strategyInfo.setStrategyName(strategyConfig.getToggleStrategyClass());
            Map<String, String> configMap = new HashMap<>(strategyConfig.getConfigurationMap());
            if (configuration != null) {
                Optional<FeatureToggleStrategyConfig> featureToggleStrategyConfigOptional = configuration.getFeatureConfiguration(feature.getFeatureId()).getFeatureToggleStrategyConfig();
                featureToggleStrategyConfigOptional.ifPresent(featureToggleStrategyConfig -> {
                    configMap.putAll(featureToggleStrategyConfig.getConfigurationMap());
                    if (configMap.containsKey("active")) {
                        strategyInfo.setActive(Boolean.parseBoolean(configMap.get("active")));
                    }
                    strategyInfo.setStrategyName(featureToggleStrategyConfig.getToggleStrategyClass());
                });
            }
            strategyInfo.setConfig(configMap);
            return strategyInfo;
        }
        else {
            return null;
        }
    }
}
