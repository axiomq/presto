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
package com.facebook.presto.features.immutable;

import com.facebook.airlift.configuration.Config;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class RateLimiterConfig
{
    private static final String RATE_LIMITER_ENABLED = "features.rate-limiter.enabled";
    private static final String RATE_LIMITER_CLASS = "features.rate-limiter.class";
    private static final String RATE_LIMITER_CURRENT_CLASS = "features.rate-limiter.current-class";
    private static final String RATE_LIMITER_IMPLEMENTATIONS = "features.rate-limiter.implementations";

    private boolean enabled;
    private String clazz;
    private String currentClazz;
    private List<String> implementations = ImmutableList.of();

    public boolean isEnabled()
    {
        return enabled;
    }

    @Config(RATE_LIMITER_ENABLED)
    public RateLimiterConfig setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public String getClazz()
    {
        return clazz;
    }

    @Config(RATE_LIMITER_CLASS)
    public RateLimiterConfig setClazz(String clazz)
    {
        this.clazz = clazz;
        return this;
    }

    public String getCurrentClazz()
    {
        return currentClazz;
    }

    @Config(RATE_LIMITER_CURRENT_CLASS)
    public RateLimiterConfig setCurrentClazz(String currentClazz)
    {
        this.currentClazz = currentClazz;
        return this;
    }

    public List<String> getImplementations()
    {
        return implementations;
    }

    @Config(RATE_LIMITER_IMPLEMENTATIONS)
    public RateLimiterConfig setImplementations(String implementations)
    {
        List<String> implementationsSplit = ImmutableList.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(implementations));
        this.implementations = implementationsSplit.stream().collect(toImmutableList());
        return this;
    }
}
