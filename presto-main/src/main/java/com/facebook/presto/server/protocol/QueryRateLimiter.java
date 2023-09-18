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
package com.facebook.presto.server.protocol;

import com.facebook.airlift.stats.TimeStat;
import com.facebook.presto.spi.QueryId;
import com.google.common.util.concurrent.ListenableFuture;
import io.airlift.units.Duration;

public interface QueryRateLimiter
{
    /*
     * For accidental bug-caused DoS, we will use delayed processing method to reduce the requests, even when user do not have back-off logic implemented
     * Optimized to avoid blocking for normal usages with TryRequire first
     * Fall back to delayed processing method to acquire a permit, in a separate thread pool
     * Internal guava rate limiter returns time spent sleeping to enforce rate, in seconds; 0.0 if not rate-limited, we use a future to wrap around that.
     */
    ListenableFuture<Double> acquire(QueryId queryId);

    TimeStat getRateLimiterBlockTime();

    void addRateLimiterBlockTime(Duration duration);
}
