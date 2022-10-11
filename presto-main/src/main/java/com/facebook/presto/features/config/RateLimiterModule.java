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

import com.facebook.airlift.configuration.AbstractConfigurationAwareModule;
import com.facebook.presto.server.protocol.QueryRateLimiter;
import com.google.inject.Binder;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Map;

public class RateLimiterModule
        extends AbstractConfigurationAwareModule
{
    @Override
    protected void setup(Binder binder)
    {
        binder.bind(QueryRateLimiter.class).toProvider(RateLimiterProvider.class);
    }

    public static class RateLimiterProvider
            implements Provider<QueryRateLimiter>
    {
        private final Map<String, Object> mapBinder;
        private final FeatureToggle featureToggle;

        @Inject
        public RateLimiterProvider(Map<String, Object> mapBinder, FeatureToggle featureToggle)
        {
            this.mapBinder = mapBinder;
            this.featureToggle = featureToggle;
        }

        @Override
        public QueryRateLimiter get()
        {
            boolean enabled = featureToggle.isFeatureEnabled(QueryRateLimiter.class);
            if (enabled) {
                return (QueryRateLimiter) mapBinder.get(featureToggle.getCurrentInstance(QueryRateLimiter.class.getName()));
            }
            else {
                return null;
            }
        }
    }
}
