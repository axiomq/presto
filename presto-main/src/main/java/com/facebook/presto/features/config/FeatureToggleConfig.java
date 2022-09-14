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

import com.facebook.airlift.configuration.Config;
import io.airlift.units.Duration;
import io.airlift.units.MinDuration;

import javax.validation.constraints.NotNull;

public class FeatureToggleConfig
{
    public static final String FEATURES_CONFIG_SOURCE = "features.config-source";
    public static final String FEATURES_CONFIG_SOURCE_TYPE = "features.config-source-type";
    public static final String FEATURES_CONFIG_TYPE = "features.config-type";
    public static final String FEATURES_REFRESH_PERIOD = "features.refresh-period";

    private String configSourceType;
    private String configSource;
    private String configType;
    private Duration refreshPeriod;

    @NotNull
    public String getConfigSource()
    {
        return configSource;
    }

    @Config(FEATURES_CONFIG_SOURCE)
    public FeatureToggleConfig setConfigSource(String configSource)
    {
        this.configSource = configSource;
        return this;
    }

    @MinDuration("1ms")
    public Duration getRefreshPeriod()
    {
        return refreshPeriod;
    }

    @Config(FEATURES_REFRESH_PERIOD)
    public FeatureToggleConfig setRefreshPeriod(Duration refreshPeriod)
    {
        this.refreshPeriod = refreshPeriod;
        return this;
    }

    public String getConfigType()
    {
        return configType;
    }

    @Config(FEATURES_CONFIG_TYPE)
    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    public String getConfigSourceType()
    {
        return configSourceType;
    }

    @Config(FEATURES_CONFIG_SOURCE_TYPE)
    public void setConfigSourceType(String configSourceType)
    {
        this.configSourceType = configSourceType;
    }
}
