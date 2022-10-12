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
package com.facebook.presto.features.binder;

import com.facebook.presto.features.config.FeatureConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Feature<T>
{
    private String featureId;
    private Class<T> baseClass;
    private FeatureConfiguration configuration;
    private PrestoFeatureToggle prestoFeatureToggle;

    public String getFeatureId()
    {
        return featureId;
    }

    public void setFeatureId(String featureId)
    {
        this.featureId = featureId;
    }

    public boolean isEnabled()
    {
        return prestoFeatureToggle.isEnabled(featureId);
    }

    public FeatureConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(FeatureConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public void setContext(PrestoFeatureToggle prestoFeatureToggle)
    {
        this.prestoFeatureToggle = prestoFeatureToggle;
    }

    public @Nullable Boolean check(Object object)
    {
        return prestoFeatureToggle.isEnabled(featureId, object);
    }

    public T getCurrentInstance(String featureId)
    {
        if (baseClass == null) {
            return null;
        }
        return baseClass.cast(prestoFeatureToggle.getCurrentInstance(featureId));
    }

    public void setBaseClass(Class<T> baseClass)
    {
        this.baseClass = baseClass;
    }
}
