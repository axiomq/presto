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
package com.facebook.presto.features;

import com.facebook.airlift.json.JsonCodec;
import com.facebook.presto.features.strategy.DefaultToggleStrategy;
import com.facebook.presto.features.strategy.FeatureToggleStrategy;
import com.facebook.presto.features.strategy.ToggleStrategyFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.facebook.airlift.json.JsonCodec.jsonCodec;
import static com.google.common.base.MoreObjects.toStringHelper;

public class FeatureConfiguration<T>
{
    private static final JsonCodec<FeatureConfiguration> FEATURE_CONFIGURATION_JSON_CODEC = jsonCodec(FeatureConfiguration.class);

    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private Class<T> featureClass;
    private FeatureToggleStrategy toggleStrategy = new DefaultToggleStrategy();

    @JsonCreator
    public FeatureConfiguration(
            @JsonProperty Class<T> featureClass,
            @JsonProperty String toggleStrategyName,
            @JsonProperty boolean enabled)
    {
        this.featureClass = featureClass;
        this.setToggleStrategy(ToggleStrategyFactory.create(toggleStrategyName));
        this.enabled.getAndSet(enabled);
        init();
    }

    public static FeatureConfiguration<?> fromJson(String json)
    {
        return FEATURE_CONFIGURATION_JSON_CODEC.fromJson(json);
    }

    private void init()
    {
        toggleStrategy.init(this);
    }

    @JsonProperty
    public Class<T> getFeatureClass()
    {
        return featureClass;
    }

    public FeatureConfiguration<T> setFeatureClass(Class<T> featureClass)
    {
        this.featureClass = featureClass;
        return this;
    }

    @JsonProperty
    public boolean isEnabled()
    {
        boolean initial = enabled.get();
        if (toggleStrategy != null) {
            return initial && toggleStrategy.evaluate();
        }
        return initial;
    }

    public FeatureConfiguration<T> setEnabled(boolean enabled)
    {
        this.enabled.getAndSet(enabled);
        return this;
    }

    @JsonProperty
    public String getToggleStrategyName()
    {
        if (toggleStrategy != null) {
            return toggleStrategy.name();
        }
        return "NONE";
    }

    public FeatureToggleStrategy getToggleStrategy()
    {
        return toggleStrategy;
    }

    public void setToggleStrategy(FeatureToggleStrategy toggleStrategy)
    {
        if (toggleStrategy != null) {
            this.toggleStrategy = toggleStrategy;
            toggleStrategy.init(this);
        }
    }

    public String toJson()
    {
        return FEATURE_CONFIGURATION_JSON_CODEC.toJson(this);
    }

    @Override
    public synchronized String toString()
    {
        return toStringHelper(this)
                .add("enabled", isEnabled())
                .add("featureClass", featureClass)
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FeatureConfiguration<?> that = (FeatureConfiguration<?>) o;

        if (!enabled.equals(that.enabled)) {
            return false;
        }
        return featureClass.equals(that.featureClass);
    }

    @Override
    public int hashCode()
    {
        int result = enabled.hashCode();
        result = 31 * result + featureClass.hashCode();
        return result;
    }
}
