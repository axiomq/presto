package com.facebook.presto.server.protocol;

import org.weakref.jmx.Managed;

public interface RetryCircuitBreakerInt
{
    void incrementFailure();

    boolean isRetryAllowed();

    @Managed
    double getRetryCount();
}
