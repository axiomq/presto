package com.facebook.presto.features.tim;

import com.facebook.presto.features.config.FeatureConfiguration;

import java.util.Map;

public class PrestoFeatureToggle
{
    Map<String, Feature<?>> featureMap;
    Map<String, Object> featureInstanceMap;
    Map<String, FeatureConfiguration> featureConfigurationMap;

    public PrestoFeatureToggle(
            Map<String, Feature<?>> featureMap,
            Map<String, Object> featureInstanceMap,
            Map<String, FeatureConfiguration> featureConfigurationMap)
    {
        this.featureMap = featureMap;
        this.featureInstanceMap = featureInstanceMap;
        this.featureConfigurationMap = featureConfigurationMap;
    }
}
