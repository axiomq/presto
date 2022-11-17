package com.facebook.presto.features.classes;

public interface Feature02
{
    default String test()
    {
        return this.getClass().getSimpleName();
    }
}
