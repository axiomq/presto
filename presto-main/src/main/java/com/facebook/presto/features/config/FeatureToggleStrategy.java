package com.facebook.presto.features.config;

import com.fasterxml.jackson.annotation.JsonCreator;

public class FeatureToggleStrategy
{

    @JsonCreator
    public FeatureToggleStrategy()
    {
    }

    public boolean check()
    {
        return true;
    }
}
