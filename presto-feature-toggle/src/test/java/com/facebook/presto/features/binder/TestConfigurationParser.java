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
package com.facebook.presto.features.binder;

import com.facebook.airlift.json.JsonObjectMapperProvider;
import com.facebook.presto.features.config.FeatureConfiguration;
import com.facebook.presto.features.config.FeatureToggleConfig;
import com.facebook.presto.features.strategy.FeatureToggleStrategyConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
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

public class TestConfigurationParser
{
    private static final String JSON = "JSON";
    private static final String PROPERTIES = "PROPERTIES";
    private static final String FEATURE = "feature";
    private static final String FEATURE_S = "feature.%s.";
    private static final String ENABLED = "enabled";
    private static final String HOT_RELOADABLE = "hot-reloadable";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String FEATURE_CLASS = "featureClass";
    private static final String FEATURE_INSTANCES = "featureInstances";
    private static final String CURRENT_INSTANCE = "currentInstance";
    private static final String DEFAULT_INSTANCE = "defaultInstance";
    private static final String STRATEGY = "strategy";
    private static final String STRATEGY_DOT = "strategy.";
    private static final String REGEX_DOT = "\\.";
    private static final String EMPTY_STRING = "";
    private static final String COMMA = ",";

    private TestConfigurationParser() {}

    public static Map<String, FeatureConfiguration> parseConfiguration(FeatureToggleConfig config)
    {
        Map<String, FeatureConfiguration> featureConfigurationMap = new ConcurrentHashMap<>();
        Path path;
        URL url = TestConfigurationParser.class.getClassLoader().getResource(config.getConfigSource());
        try {
            assert url != null;
            path = Paths.get(url.toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        checkArgument(exists(path), "File does not exist: %s", path);
        checkArgument(isReadable(path), "File is not readable: %s", path);
        if (JSON.equalsIgnoreCase(config.getConfigType())) {
            parseJson(path, featureConfigurationMap);
        }
        else if (PROPERTIES.equalsIgnoreCase(config.getConfigType())) {
            parseProperties(path, featureConfigurationMap);
        }
        return featureConfigurationMap;
    }

    private static void parseProperties(Path path, Map<String, FeatureConfiguration> featureConfigurationMap)
    {
        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(path));
            Map<String, Map<String, String>> map = new TreeMap<>();
            for (Object key : properties.keySet()) {
                String propertyKey = (String) key;
                List<String> keyList = Arrays.asList(propertyKey.split(REGEX_DOT));
                if (!FEATURE.equals(keyList.get(0))) {
                    continue;
                }
                String featureId = keyList.get(1);
                if (!map.containsKey(featureId)) {
                    map.put(featureId, new TreeMap<>());
                }
                String property = propertyKey.replace(format(FEATURE_S, featureId), EMPTY_STRING);
                map.get(featureId).put(property, properties.getProperty(propertyKey));
            }
            map.keySet().forEach(featureId -> {
                Map<String, String> featureMap = map.get(featureId);
                featureConfigurationMap.put(featureId, new FeatureConfiguration(
                        featureId,
                        Boolean.parseBoolean(featureMap.getOrDefault(ENABLED, TRUE)),
                        Boolean.parseBoolean(featureMap.getOrDefault(HOT_RELOADABLE, FALSE)),
                        featureMap.get(FEATURE_CLASS),
                        Arrays.asList(featureMap.getOrDefault(FEATURE_INSTANCES, EMPTY_STRING).split(COMMA)),
                        featureMap.get(CURRENT_INSTANCE),
                        featureMap.get(DEFAULT_INSTANCE),
                        parseStrategy(featureMap)));
            });
        }
        catch (IOException e) {
            throw new IllegalArgumentException(format("Invalid Properties file '%s'", path), e);
        }
    }

    private static FeatureToggleStrategyConfig parseStrategy(Map<String, String> featureMap)
    {
        if (!featureMap.containsKey(STRATEGY)) {
            return null;
        }
        Map<String, String> strategyMap = new ConcurrentHashMap<>();
        featureMap.keySet().stream()
                .filter(key -> key.startsWith(STRATEGY))
                .forEach(key -> strategyMap.put(key.replace(STRATEGY_DOT, EMPTY_STRING), featureMap.get(key)));

        return new FeatureToggleStrategyConfig(featureMap.get(STRATEGY), strategyMap);
    }

    private static void parseJson(Path path, Map<String, FeatureConfiguration> featureConfigurationMap)
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
    }

    public static void updateProperty(FeatureToggleConfig config, String key, String value)
    {
        Properties props = new Properties();
        URL url = TestConfigurationParser.class.getClassLoader().getResource(config.getConfigSource());
        assert url != null;
        try (InputStream in = Files.newInputStream(Paths.get(url.toURI()))) {
            props.load(in);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (OutputStream out = Files.newOutputStream(Paths.get(url.toURI()))) {
            props.setProperty(key, value);
            props.store(out, null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = Files.newInputStream(Paths.get(url.toURI()))) {
            props.load(in);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
