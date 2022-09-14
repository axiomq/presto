package com.facebook.presto.server.protocol;

import com.facebook.airlift.stats.TimeStat;
import com.facebook.presto.spi.QueryId;
import com.google.common.util.concurrent.ListenableFuture;
import io.airlift.units.Duration;

public interface QueryRateLimiter
{
    TimeStat getRateLimiterBlockTime();

    ListenableFuture<Double> acquire(QueryId queryId);

    void addRateLimiterBlockTime(Duration duration);
}
