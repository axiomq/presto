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
package com.facebook.presto.features.strategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;

public class FeatureToggleStrategyConfig
{
    private final Map<String, String> configurationMap;
    private final String strategyClass;

    @JsonCreator
    public FeatureToggleStrategyConfig(
            @JsonProperty String strategyClass,
            @JsonProperty Map<String, String> configurationMap)
    {
        this.strategyClass = strategyClass;
        this.configurationMap = configurationMap;
    }

    public boolean active()
    {
        if(configurationMap.containsKey("active")){
            return Boolean.parseBoolean(configurationMap.get("active"));
        }
        return true;
    }

    public String getToggleStrategyClass()
    {
        return strategyClass;
    }

    public Optional<String> get(String key)
    {
        return Optional.ofNullable(configurationMap.get(key));
    }
}
