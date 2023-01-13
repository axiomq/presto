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
import com.facebook.presto.features.config.FeatureToggleConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ThriftStruct
public class FeatureInfo
{
    private String featureId;
    private boolean enabled;
    private String featureClass;
    private List<String> featureInstances;
    private String currentInstance;
    private String defaultInstance;
    private FeatureStrategyInfo strategy;

    @JsonProperty
    @ThriftField(1)
    public String getFeatureId()
    {
        return featureId;
    }

    public void setFeatureId(String featureId)
    {
        this.featureId = featureId;
    }

    @JsonProperty
    @ThriftField(1)
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @JsonProperty
    @ThriftField(1)
    public String getFeatureClass()
    {
        return featureClass;
    }

    public void setFeatureClass(String featureClass)
    {
        this.featureClass = featureClass;
    }

    @JsonProperty
    @ThriftField(1)
    public List<String> getFeatureInstances()
    {
        return featureInstances;
    }

    public void setFeatureInstances(List<String> featureInstances)
    {
        this.featureInstances = featureInstances;
    }

    @JsonProperty
    @ThriftField(1)
    public String getCurrentInstance()
    {
        return currentInstance;
    }

    public void setCurrentInstance(String currentInstance)
    {
        this.currentInstance = currentInstance;
    }

    @JsonProperty
    @ThriftField(1)
    public String getDefaultInstance()
    {
        return defaultInstance;
    }

    public void setDefaultInstance(String defaultInstance)
    {
        this.defaultInstance = defaultInstance;
    }

    @JsonProperty
    @ThriftField(1)
    public FeatureStrategyInfo getStrategy()
    {
        return strategy;
    }

    public void setStrategy(FeatureStrategyInfo strategy)
    {
        this.strategy = strategy;
    }

    public static List<FeatureInfo> featureToggle(PrestoFeatureToggle prestoFeatureToggle)
    {
        Map<String, Feature<?>> featureMap = prestoFeatureToggle.getFeatureMap();

        return featureMap.values().stream()
                .map(feature -> getFeatureInfo(prestoFeatureToggle, feature))
                .collect(Collectors.toList());
    }

    public static FeatureInfo getFeatureInfo(PrestoFeatureToggle prestoFeatureToggle, Feature<?> feature)
    {
        FeatureToggleConfiguration configuration = prestoFeatureToggle.getFeatureToggleConfiguration();
        String featureId = feature.getFeatureId();
        FeatureInfo info = new FeatureInfo();
        info.setFeatureId(featureId);
        info.setEnabled(prestoFeatureToggle.isEnabled(featureId));
        info.setFeatureClass(feature.getConfiguration().getFeatureClass());
        info.setDefaultInstance(feature.getConfiguration().getDefaultInstance());
        Object featureCurrentInstance = feature.getCurrentInstance(featureId);
        String currentInstanceClass = null;
        if (featureCurrentInstance != null) {
            currentInstanceClass = featureCurrentInstance.getClass().getName();
        }
        info.setCurrentInstance(currentInstanceClass);
        info.setFeatureInstances(feature.getConfiguration().getFeatureInstances());
        if (feature.getConfiguration().getFeatureToggleStrategyConfig().isPresent()) {
            info.setStrategy(FeatureStrategyInfo.strategy(feature, configuration));
        }
        return info;
    }
}
