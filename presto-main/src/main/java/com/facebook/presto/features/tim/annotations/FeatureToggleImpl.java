package com.facebook.presto.features.tim.annotations;

import com.google.inject.internal.Annotations;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

public class FeatureToggleImpl
        implements FeatureToggle, Serializable
{

    private final String value;

    public FeatureToggleImpl(String value)
    {
        this.value = checkNotNull(value, "name");
    }

    @Override
    public String value()
    {
        return this.value;
    }

    @Override
    public int hashCode()
    {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof FeatureToggle)) {
            return false;
        }

        FeatureToggle other = (FeatureToggle) o;
        return value.equals(other.value());
    }

    @Override
    public String toString()
    {
        return "@" + FeatureToggle.class.getName() + "(value=" + Annotations.memberValueString(value) + ")";
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
        return FeatureToggle.class;
    }

    private static final long serialVersionUID = 0;
}
