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

import com.facebook.airlift.json.JsonCodec;
import com.facebook.presto.features.FeatureConfiguration;
import com.facebook.presto.features.FeatureInstance;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.facebook.airlift.json.JsonCodec.jsonCodec;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

public class Feature<T>
        implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final JsonCodec<Feature> FEATURE_JSON_CODEC = jsonCodec(Feature.class);

    private final String featureId;
    private final Set<T> instances = new HashSet<>();
    private final FeatureConfiguration<T> configuration;

    @JsonCreator
    public Feature(
            @JsonProperty("featureId") String featureId,
            @JsonProperty("configuration") com.facebook.presto.features.FeatureConfiguration<T> configuration)
    {
        this.featureId = requireNonNull(featureId, "featureId is null");
        this.configuration = configuration;
    }

    public static Feature<?> fromJson(String json)
    {
        return FEATURE_JSON_CODEC.fromJson(json);
    }

    @JsonProperty
    public String getFeatureId()
    {
        return featureId;
    }

    @JsonProperty
    public boolean isEnabled()
    {
        return getConfiguration().isEnabled();
    }

    @JsonProperty
    public FeatureConfiguration<T> getConfiguration()
    {
        return configuration;
    }

    public String toJson()
    {
        return FEATURE_JSON_CODEC.toJson(this);
    }

    public Set<T> getInstances()
    {
        return instances;
    }

    public void addInstance(T instance)
    {
        instances.add(instance);
    }

    public FeatureInstance<T> getDefaultInstance()
    {
        return null;
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
        Feature<?> that = (Feature<?>) o;
        return Objects.equals(featureId, that.featureId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(featureId);
    }

    @Override
    public synchronized String toString()
    {
        return toStringHelper(this)
                .add("featureId", featureId)
                .add("enabled", isEnabled())
                .toString();
    }
}
