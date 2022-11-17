package com.facebook.presto.features.classes;

public interface Feature01
{    default String test(){
        return this.getClass().getSimpleName();
    }
}
