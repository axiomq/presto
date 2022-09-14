package com.facebook.presto.features.config;

import com.facebook.airlift.json.JsonObjectMapperProvider;
import com.facebook.airlift.log.Logger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isReadable;

public class FeatureConfigurationParser
{

    private static final Logger log = Logger.get(FeatureConfigurationParser.class);

    private static HashMap<String, FeatureConfiguration> parseConfiguration(FeatureToggleConfig config)
    {
        if ("JSON".equalsIgnoreCase(config.getConfigType())) {
            return parseJson(config);
        }
        else if ("PROPERTIES".equalsIgnoreCase(config.getConfigType())) {
            return parseProperties(config);
        }
        return null;
    }

    private static HashMap<String, FeatureConfiguration> parseProperties(FeatureToggleConfig config)
    {
        return null;
    }

    private static HashMap<String, FeatureConfiguration> parseJson(FeatureToggleConfig config)
    {
        HashMap<String, FeatureConfiguration> featureConfigurationMap = new HashMap<>();
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
        return featureConfigurationMap;
    }
}
