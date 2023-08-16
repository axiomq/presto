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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.facebook.presto.features.config.ConfigurationParser.parseJsonConfiguration;
import static com.facebook.presto.features.config.ConfigurationParser.parsePropertiesConfiguration;
import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isReadable;

public class TestConfigurationParser
{
    private static final String JSON = "JSON";
    private static final String PROPERTIES = "PROPERTIES";

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
            parseJsonConfiguration(path, featureConfigurationMap);
        }
        else if (PROPERTIES.equalsIgnoreCase(config.getConfigType())) {
            parsePropertiesConfiguration(path, featureConfigurationMap);
        }
        return featureConfigurationMap;
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
