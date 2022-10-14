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
package com.facebook.presto.features;

import com.facebook.presto.features.FeatureToggle;
import com.facebook.presto.features.config.FeatureConfiguration;
import com.facebook.presto.features.config.FeatureToggleConfig;

import java.util.Collection;
import java.util.Map;

import static com.facebook.presto.features.config.ConfigurationParser.parseConfiguration;

public class FileBasedFeatureToggle
        implements FeatureToggle
{
    private final Map<String, FeatureConfiguration> featureConfigurationMap;

    public FileBasedFeatureToggle(FeatureToggleConfig config)
    {
        this.featureConfigurationMap = parseConfiguration(config);
    }

    @Override
    public <T> boolean isFeatureEnabled(Class<T> clazz)
    {
        FeatureConfiguration configuration = featureConfigurationMap.get(clazz.getName());
        if (configuration != null) {
            return configuration.isEnabled();
        }
        else {
            return true;
        }
    }

    @Override
    public Collection<FeatureConfiguration> getFeatureConfigurations()
    {
        return featureConfigurationMap.values();
    }

    @Override
    public String getCurrentInstance(String featureClassName)
    {
        return featureConfigurationMap.get(featureClassName).getCurrentInstance();
    }
}
