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

import com.facebook.presto.features.strategy.FeatureToggleStrategy;
import com.facebook.presto.features.strategy.FeatureToggleStrategyFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.facebook.presto.features.config.ConfigurationParser.parseConfiguration;

public class FileBasedFeatureToggleConfiguration
        implements FeatureToggleConfiguration
{
    private final Map<String, FeatureConfiguration> featureConfigurationMap;

    public FileBasedFeatureToggleConfiguration(FeatureToggleConfig config)
    {
        this.featureConfigurationMap = parseConfiguration(config);
    }

    @Override
    public FeatureConfiguration getFeatureConfiguration(String featureId)
    {
        return featureConfigurationMap.get(featureId);
    }

    @Override
    public String getCurrentInstance(String featureId)
    {
        return getFeatureConfiguration(featureId).getCurrentInstance();
    }
}
