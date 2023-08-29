package com.facebook.presto.features.config;

import java.util.Map;

public interface FeatureToggleConfigurationSource
{
    Map<String, FeatureConfiguration> parseConfiguration(FeatureToggleConfig config);
}
