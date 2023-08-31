package com.facebook.presto.features.plugin;

import java.util.Map;

public interface ConfigurationSourceFactory
{
    String getName();

    ConfigrationSource create(String catalogName, Map<String, String> requiredConfig, ConnectorContext context);
}
