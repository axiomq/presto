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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isReadable;

public class ConfigurationParser
{
    private static final Logger log = Logger.get(FileBasedFeatureToggle.class);

    private ConfigurationParser() {}

    public static Map<String, FeatureConfiguration> parseConfiguration(FeatureToggleConfig config)
    {
        Map<String, FeatureConfiguration> featureConfigurationMap = new ConcurrentHashMap<>();
        log.info("parsing configuration %s", config.getConfigSource());
        Path path = Paths.get(config.getConfigSource());

        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        checkArgument(exists(path), "File does not exist: %s", path);
        checkArgument(isReadable(path), "File is not readable: %s", path);
        if ("JSON".equalsIgnoreCase(config.getConfigType())) {
            featureConfigurationMap = parseJson(path, featureConfigurationMap);
        }
        else if ("PROPERTIES".equalsIgnoreCase(config.getConfigType())) {
            featureConfigurationMap = parseProperties(path, featureConfigurationMap);
        }
        return featureConfigurationMap;
    }

    private static Map<String, FeatureConfiguration> parseProperties(Path path, Map<String, FeatureConfiguration> featureConfigurationMap)
    {
        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(path));
            Map<String, Map<String, String>> map = new TreeMap<>();
            for (Object key : properties.keySet()) {
                String propertyKey = (String) key;
                List<String> keyList = Arrays.asList(propertyKey.split("\\."));
                if (!"feature".equals(keyList.get(0))) {
                    continue;
                }
                String featureId = keyList.get(1);
                if (!map.containsKey(featureId)) {
                    map.put(featureId, new TreeMap<>());
                }
                String property = keyList.get(2);
                map.get(featureId).put(property, properties.getProperty(propertyKey));
            }
            map.keySet().forEach(feature -> {
                Map<String, String> featureMap = map.get(feature);
                featureConfigurationMap.put(featureMap.get("featureClass"), new FeatureConfiguration(
                        featureMap.get("featureId"),
                        Boolean.parseBoolean(featureMap.get("enabled")),
                        featureMap.get("featureClass"),
                        Arrays.asList(featureMap.get("featureInstances").split(",")),
                        featureMap.get("currentInstance"),
                        null));
            });
        }
        catch (IOException e) {
            throw new IllegalArgumentException(format("Invalid Properties file '%s'", path), e);
        }
        return featureConfigurationMap;
    }

    private static Map<String, FeatureConfiguration> parseJson(Path path, Map<String, FeatureConfiguration> featureConfigurationMap)
    {
        try {
            ObjectMapper mapper = new JsonObjectMapperProvider().get()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            List<FeatureConfiguration> configurationList = Arrays.asList(mapper.readValue(path.toFile(), FeatureConfiguration[].class));
            configurationList.forEach(f ->
                    featureConfigurationMap.put(f.getFeatureClass(), f));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(format("Invalid JSON file '%s'", path), e);
        }
        return featureConfigurationMap;
    }
}
