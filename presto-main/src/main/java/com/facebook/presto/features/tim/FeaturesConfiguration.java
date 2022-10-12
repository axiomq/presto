package com.facebook.presto.features.tim;

import com.facebook.presto.features.config.FeatureConfiguration;

import java.util.Map;

public class FeaturesConfiguration
{
    Map<String, FeatureConfiguration> featureConfigurationMap;

    public FeaturesConfiguration(Map<String, FeatureConfiguration> featureConfigurationMap)
    {
        this.featureConfigurationMap = featureConfigurationMap;
    }




}
