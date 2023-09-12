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

import com.facebook.presto.spi.features.ConfigurationSource;
import com.facebook.presto.spi.features.FeatureConfiguration;
import com.facebook.presto.spi.features.FeatureToggleConfiguration;

import java.util.Map;

public class TestConfigurationSource
        implements ConfigurationSource
{
    public static final String NAME = "test";

    private final Map<String, FeatureConfiguration> featureConfigurationMap;

    public TestConfigurationSource(Map<String, FeatureConfiguration> featureConfigurationMap)
    {
        this.featureConfigurationMap = featureConfigurationMap;
    }

    @Override
    public FeatureToggleConfiguration getConfiguration()
    {
        return new DefaultFeatureToggleConfiguration(featureConfigurationMap);
    }
}
