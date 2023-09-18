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
package com.facebook.presto.features.plugin;

import com.facebook.airlift.log.Logger;
import com.facebook.presto.features.config.FeatureToggleConfig;
import com.facebook.presto.spi.features.ConfigurationSource;
import com.facebook.presto.spi.features.ConfigurationSourceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.facebook.airlift.configuration.ConfigurationLoader.loadPropertiesFrom;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FeatureToggleConfigurationManager
{
    public static final String FEATURE_TOGGLE_CONFIGURATION_FACTORY_NAME = "features.config-source-type";
    private static final Logger log = Logger.get(FeatureToggleConfigurationManager.class);
    private static final File FEATURE_TOGGLE_CONFIGURATION_DIR = new File("etc/feature-toggle/");
    private final Map<String, ConfigurationSourceFactory> configurationSourceFactories = new ConcurrentHashMap<>();
    private final Map<String, ConfigurationSource> loadedConfigurationSources = new ConcurrentHashMap<>();
    private final AtomicBoolean tempStorageLoading = new AtomicBoolean();
    private String configType;
    private String configDir;

    //
    public FeatureToggleConfigurationManager()
    {
    }

    //
    @Inject
    public FeatureToggleConfigurationManager(FeatureToggleConfig featureToggleConfig)
    {
        requireNonNull(featureToggleConfig, "Feature Toggle Config is null");
        this.configType = requireNonNull(featureToggleConfig.getConfigType(), "Feature Toggle Config Type is null");
        this.configDir = requireNonNull(featureToggleConfig.getConfigDirectory(), "Feature Toggle Config Directory is null");
    }

    private static List<File> listFiles(File dir)
    {
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                return ImmutableList.copyOf(files);
            }
        }
        return ImmutableList.of();
    }

    public void addConfigurationSourceFactory(ConfigurationSourceFactory configurationSourceFactory)
    {
        requireNonNull(configurationSourceFactory, "configurationSourceFactory is null");
        if (configurationSourceFactories.putIfAbsent(configurationSourceFactory.getName(), configurationSourceFactory) != null) {
            throw new IllegalArgumentException(format("Feature Toggle Configuration Source '%s' is already registered", configurationSourceFactory.getName()));
        }
    }

    public ConfigurationSource getConfigurationSource(String name)
    {
        ConfigurationSource configurationSource = loadedConfigurationSources.get(name);
        checkState(configurationSource != null, "configurationSource %s was not loaded", name);
        return configurationSource;
    }

    public void loadConfigurationSources()
            throws IOException
    {
        ImmutableMap.Builder<String, Map<String, String>> configurationProperties = ImmutableMap.builder();
        log.debug(format("Feature Toggle Config Directory = %s", configDir));
        for (File file : listFiles(FEATURE_TOGGLE_CONFIGURATION_DIR)) {
            if (file.isFile() && file.getName().endsWith(".properties")) {
                String name = getNameWithoutExtension(file.getName());
                Map<String, String> properties = loadPropertiesFrom(file.getPath());
                configurationProperties.put(name, properties);
            }
        }
        loadConfigurationSources(configurationProperties.build());
    }

    public void loadConfigurationSources(Map<String, Map<String, String>> storageProperties)
            throws IOException
    {
        if (!tempStorageLoading.compareAndSet(false, true)) {
            return;
        }
        storageProperties.forEach(this::loadConfigurationSource);
    }

    protected void loadConfigurationSource(String name, Map<String, String> properties)
    {
        requireNonNull(name, "name is null");
        requireNonNull(properties, "properties is null");

        log.info("-- Loading configuration source %s --", name);

        String configurationSourceFactoryName = null;
        ImmutableMap.Builder<String, String> configurationSourceProperties = ImmutableMap.builder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equals(FEATURE_TOGGLE_CONFIGURATION_FACTORY_NAME)) {
                configurationSourceFactoryName = entry.getValue();
            }
            else {
                configurationSourceProperties.put(entry.getKey(), entry.getValue());
            }
        }
        checkState(configurationSourceFactoryName != null, "Configuration for Configuration source %s does not contain feature.configuration.name", name);

        ConfigurationSourceFactory factory = configurationSourceFactories.get(configurationSourceFactoryName);
        checkState(factory != null, "Configuration Source Factory %s is not registered", configurationSourceFactoryName);

        ConfigurationSource configurationSource = factory.create(configurationSourceProperties.build());
        if (loadedConfigurationSources.putIfAbsent(name, configurationSource) != null) {
            throw new IllegalArgumentException(format("Configuration Source '%s' is already loaded", name));
        }

        log.info("-- Loaded Configuration Source %s --", name);
    }
}
