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
import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.config.FeatureConfiguration;
import com.facebook.presto.features.config.FeatureToggleConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

@ThriftStruct
public class FeatureDetails
{
    private FeatureInfo initialConfiguration;
    private FeatureInfo configurationOverride;
    private FeatureInfo activeConfiguration;

    @JsonProperty
    @ThriftField(1)
    public FeatureInfo getInitialConfiguration()
    {
        return initialConfiguration;
    }

    public void setInitialConfiguration(FeatureInfo initialConfiguration)
    {
        this.initialConfiguration = initialConfiguration;
    }

    @JsonProperty
    @ThriftField(2)
    public FeatureInfo getConfigurationOverride()
    {
        return configurationOverride;
    }

    public void setConfigurationOverride(FeatureInfo configurationOverride)
    {
        this.configurationOverride = configurationOverride;
    }

    @JsonProperty
    @ThriftField(3)
    public FeatureInfo getActiveConfiguration()
    {
        return activeConfiguration;
    }

    public void setActiveConfiguration(FeatureInfo activeConfiguration)
    {
        this.activeConfiguration = activeConfiguration;
    }

    public static FeatureDetails details(PrestoFeatureToggle featureToggle, String featureId, FeatureToggleConfiguration configuration)
    {
        Feature<?> feature = featureToggle.getFeatureMap().get(featureId);
        FeatureInfo initialFeatureInfo = new FeatureInfo();
        initialFeatureInfo.setFeatureId(featureId);
        initialFeatureInfo.setEnabled(feature.getConfiguration().isEnabled());
        initialFeatureInfo.setFeatureClass(feature.getConfiguration().getFeatureClass());
        initialFeatureInfo.setDefaultInstance(feature.getConfiguration().getDefaultInstance());
        initialFeatureInfo.setCurrentInstance(feature.getConfiguration().getCurrentInstance());
        if (feature.getConfiguration().getFeatureToggleStrategyConfig().isPresent()) {
            initialFeatureInfo.setStrategy(FeatureStrategyInfo.strategy(feature, null));
        }
        FeatureDetails featureDetails = new FeatureDetails();
        featureDetails.setInitialConfiguration(initialFeatureInfo);

        FeatureConfiguration featureConfigurationOverride = configuration.getFeatureConfiguration(featureId);
        FeatureInfo overrideFeatureInfo = new FeatureInfo();
        overrideFeatureInfo.setFeatureId(featureId);
        overrideFeatureInfo.setEnabled(featureConfigurationOverride.isEnabled());
        String currentInstanceClass = featureConfigurationOverride.getCurrentInstance();
        if (currentInstanceClass != null) {
            overrideFeatureInfo.setCurrentInstance(currentInstanceClass);
        }
        if (featureConfigurationOverride.getFeatureToggleStrategyConfig().isPresent()) {
            overrideFeatureInfo.setStrategy(FeatureStrategyInfo.strategy(feature, configuration));
        }
        featureDetails.setConfigurationOverride(overrideFeatureInfo);

        featureDetails.setActiveConfiguration(FeatureInfo.getFeatureInfo(featureToggle, feature));
        return featureDetails;
    }
}
