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
    private final String description;
    private final Set<FeatureInstance<T>> instances = new HashSet<>();
    private final FeatureConfiguration<T> configuration;

    @JsonCreator
    public Feature(
            @JsonProperty("featureId") String featureId,
            @JsonProperty("description") String description,
            @JsonProperty("configuration") FeatureConfiguration<T> configuration)
    {
        this.featureId = requireNonNull(featureId, "featureId is null");
        this.description = requireNonNull(description, "description is null");
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
    public String getDescription()
    {
        return description;
    }

    @JsonProperty
    public boolean isEnabled()
    {
        return getConfiguration().isEnabled();
    }

    public void disable()
    {
        getConfiguration().setEnabled(false);
    }

    public void enable()
    {
        getConfiguration().setEnabled(true);
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

    public Set<FeatureInstance<T>> getInstances()
    {
        return instances;
    }

    public void addInstance(FeatureInstance<T> instance)
    {
        if (hasInstance(instance.getInstanceId())) {
            throw new IllegalArgumentException("instance with given id already registered");
        }
        if (instance.isDefault()) {
            addDefaultInstance(instance);
        }
        else {
            instances.add(instance);
        }
    }

    public void addDefaultInstance(FeatureInstance<T> instance)
    {
        instances.forEach(i -> i.setDefault(false));
        instances.add(instance);
    }

    public FeatureInstance<T> getDefaultInstance()
    {
        return instances.stream().filter(FeatureInstance::isDefault).findFirst().orElseThrow(RuntimeException::new);
    }

    public void changeDefaultInstance(String instanceId)
    {
        if (hasInstance(instanceId)) {
            instances.forEach(i -> i.setDefault(Objects.equals(i.getInstanceId(), instanceId)));
        }
        FeatureInstance<T> defaultInstance = getDefaultInstance();
        System.out.println(defaultInstance);
    }

    private boolean hasInstance(String instanceId)
    {
        return instances.stream().anyMatch(i -> Objects.equals(i.getInstanceId(), instanceId));
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
                .add("description", description)
                .add("enabled", isEnabled())
                .toString();
    }
}
