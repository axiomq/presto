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
import com.facebook.presto.features.tim.annotations.FeatureToggles;
import com.facebook.presto.server.protocol.AnotherQueryBlockingRateLimiter;
import com.facebook.presto.server.protocol.QueryBlockingRateLimiter;
import com.facebook.presto.server.protocol.QueryRateLimiter;
import com.facebook.presto.server.protocol.RetryCircuitBreaker;
import com.facebook.presto.server.protocol.RetryCircuitBreakerInt;
import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Map;

public class RateLimiterModule
        extends AbstractConfigurationAwareModule
{
    private static final String QUERY_RATE_LIMITER = "query-rate-limiter";

    @Override
    protected void setup(Binder binder)
    {
        binder.bind(QueryRateLimiter.class).toProvider(RateLimiterProvider.class);
        binder.bind(QueryRateLimiter.class).annotatedWith(FeatureToggles.named("query-rate-limiter-default")).to(QueryBlockingRateLimiter.class);
        binder.bind(RetryCircuitBreakerInt.class).annotatedWith(FeatureToggles.named("circuit-breaker")).to(RetryCircuitBreaker.class).in(Singleton.class);
    }

    public static class RateLimiterProvider
            implements Provider<QueryRateLimiter>
    {
        private final Map<String, Object> featureMapBinder;
        private final FeatureToggle featureToggle;

        @Inject
        public RateLimiterProvider(Map<String, Object> featureMapBinder, FeatureToggle featureToggle)
        {
            this.featureMapBinder = featureMapBinder;
            this.featureToggle = featureToggle;
        }

        @Override
        public QueryRateLimiter get()
        {
            boolean enabled = featureToggle.check(QUERY_RATE_LIMITER);
            if (enabled) {
                return (QueryRateLimiter) featureMapBinder.get(featureToggle.getCurrentInstance(QueryRateLimiter.class.getName()));
            }
            else {
                return (QueryRateLimiter) featureMapBinder.get(featureToggle.getDefaultInstance(QueryRateLimiter.class.getName()));
            }
        }
    }
}
