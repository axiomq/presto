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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFeatures
        implements Features
{
    private final Map<String, Feature<?>> features = new ConcurrentHashMap<>();

    public <T> void addFeature(String featureClassName, Feature<T> featureInstance)
            throws ClassNotFoundException
    {
        Class<?> cl = Class.forName(featureClassName);
        features.put(cl.getName(), featureInstance);
    }

    @Override
    public Feature<?> getFeature(String featureName)
    {
        Feature<?> feature = features.get(featureName);
        if (feature == null) {
            throw new IllegalStateException("The feature has not yet been registered: " + featureName);
        }
        return feature;
    }

    @Override
    public <T> T getFeature(String featureName, Class<T> featureClass)
    {
        Feature<?> feature = features.get(featureName);
        if (feature == null) {
            throw new IllegalStateException("The feature has not yet been registered: " + featureClass.getCanonicalName());
        }
        return featureClass.cast(feature.getDefaultInstance().getInstance());
    }

    public <T> void addFeature(Feature<T> feature)
    {
        features.put(feature.getFeatureId(), feature);
    }

    @Override
    public boolean checkFeature(String featureId)
    {
        Feature<?> feature = features.get(featureId);
        if (feature == null) {
            throw new IllegalStateException("The feature has not yet been registered: " + featureId);
        }
        return feature.isEnabled();
    }

    @Override
    public Collection<Feature<?>> getAll()
    {
        return features.values();
    }
}
