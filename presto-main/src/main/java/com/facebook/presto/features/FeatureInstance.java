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

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

public class FeatureInstance<T>
{
    private final T instance;
    private boolean isDefault;
    private String instanceId;

    public FeatureInstance(T instance, String instanceId)
    {
        this(instance, instanceId, false);
    }

    public FeatureInstance(T instance, String instanceId, boolean isDefault)
    {
        this.instance = instance;
        this.isDefault = isDefault;
        this.instanceId = instanceId;
    }

    public T getInstance()
    {
        return instance;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public void setDefault(boolean aDefault)
    {
        isDefault = aDefault;
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
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
        FeatureInstance that = (FeatureInstance) o;
        return Objects.equals(instance, that.instance);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(instance);
    }

    @Override
    public synchronized String toString()
    {
        return toStringHelper(this)
                .add("instance", instance)
                .add("default", isDefault())
                .toString();
    }
}
