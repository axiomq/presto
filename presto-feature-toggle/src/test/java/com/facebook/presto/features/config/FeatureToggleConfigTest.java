package com.facebook.presto.features.config;

import com.facebook.airlift.configuration.ConfigurationFactory;
import com.facebook.airlift.configuration.testing.ConfigAssertions;
import com.google.common.collect.ImmutableMap;
import io.airlift.units.Duration;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.facebook.airlift.configuration.testing.ConfigAssertions.assertFullMapping;
import static com.facebook.airlift.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static com.facebook.presto.features.config.FeatureToggleConfig.FEATURES_CONFIG_SOURCE;
import static com.facebook.presto.features.config.FeatureToggleConfig.FEATURES_CONFIG_SOURCE_TYPE;
import static com.facebook.presto.features.config.FeatureToggleConfig.FEATURES_CONFIG_TYPE;
import static com.facebook.presto.features.config.FeatureToggleConfig.FEATURES_REFRESH_PERIOD;

public class FeatureToggleConfigTest
{
    private static FeatureToggleConfig newInstance(Map<String, String> properties)
    {
        ConfigurationFactory configurationFactory = new ConfigurationFactory(properties);
        return configurationFactory.build(FeatureToggleConfig.class);
    }

    @Test
    public void testDefaults()
    {
        assertRecordedDefaults(ConfigAssertions.recordDefaults(FeatureToggleConfig.class)
                .setConfigSource(null)
                .setConfigSourceType(null)
                .setConfigType(null)
                .setRefreshPeriod(null));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put(FEATURES_CONFIG_SOURCE, "/test.json")
                .put(FEATURES_REFRESH_PERIOD, "1s")
                .put(FEATURES_CONFIG_TYPE, "json")
                .put(FEATURES_CONFIG_SOURCE_TYPE, "file")
                .build();

        FeatureToggleConfig expected = new FeatureToggleConfig()
                .setConfigSource("/test.json")
                .setRefreshPeriod(new Duration(1, TimeUnit.SECONDS))
                .setConfigSourceType("file")
                .setConfigType("json");

        assertFullMapping(properties, expected);
    }
}
