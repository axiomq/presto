package com.facebook.presto.features.config;

import java.util.Map;

public class FeatureToggleStrategyConfig
{
    private String name;
    private Map<String, String> strategyConfig;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getStrategyConfig()
    {
        return strategyConfig;
    }

    public void setStrategyConfig(Map<String, String> strategyConfig)
    {
        this.strategyConfig = strategyConfig;
    }
}
