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
package com.facebook.presto.features.config;

import com.facebook.airlift.json.JsonObjectMapperProvider;
import com.facebook.airlift.log.Logger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isReadable;

public class FileBasedFeatureToggle
        implements FeatureToggle
{

    private static final Logger log = Logger.get(FileBasedFeatureToggle.class);

    Map<String, FeatureConfiguration> featureConfigurationMap = new ConcurrentHashMap<>();

    public FileBasedFeatureToggle(FeatureToggleConfig config)
    {
        parseConfiguration(config);
    }

    private void parseConfiguration(FeatureToggleConfig config)
    {
        if ("JSON".equalsIgnoreCase(config.getConfigType())) {
            parseJson(config);
        }
        else if ("PROPERTIES".equalsIgnoreCase(config.getConfigType())) {
            parseProperties(config);
        }
    }

    private void parseProperties(FeatureToggleConfig config)
    {

    }

    private void parseJson(FeatureToggleConfig config)
    {
        log.info("parsing configuration %s", config.getConfigSource());
        Path path = Paths.get(config.getConfigSource());

        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        checkArgument(exists(path), "File does not exist: %s", path);
        checkArgument(isReadable(path), "File is not readable: %s", path);
        try {
            ObjectMapper mapper = new JsonObjectMapperProvider().get()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            List<FeatureConfiguration> configurationList = Arrays.asList(mapper.readValue(path.toFile(), FeatureConfiguration[].class));
            configurationList.forEach(f ->
                    featureConfigurationMap.put(f.getFeatureClass(), f)
            );
        }
        catch (IOException e) {
            throw new IllegalArgumentException(format("Invalid JSON file '%s'", path), e);
        }
    }

    @Override
    public <T> boolean isFeatureEnabled(Class<T> clazz)
    {
        FeatureConfiguration configuration = featureConfigurationMap.get(clazz.getName());
        if (configuration != null) {
            return configuration.isEnabled();
        }
        else {
            return true;
        }
    }

    @Override
    public Collection<FeatureConfiguration> getFeatureConfigurations()
    {
        return featureConfigurationMap.values();
    }
}
