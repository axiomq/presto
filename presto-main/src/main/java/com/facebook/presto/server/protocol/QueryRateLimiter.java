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
