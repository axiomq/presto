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

import com.facebook.presto.features.store.FeatureStore;
import com.google.inject.Inject;

public class FeatureToggle
{
    private Features features = new DefaultFeatures();

    private FeatureStore store;

    public FeatureToggle()
    {
    }

    public Features getFeatures()
    {
        return features;
    }

    @Inject
    public void setFeatures(Features features)
    {
        this.features = features;
    }

    public FeatureStore getStore()
    {
        return store;
    }

    public void setStore(FeatureStore store)
    {
        this.store = store;
    }

    public void addFeature(Feature<?> feature)
    {
        store.store(feature.getFeatureId(), feature);
        ((DefaultFeatures) features).addFeature(feature);
    }

    public boolean checkFeature(String featureId)
    {
        Feature<?> feature = features.getFeature(featureId);
        return feature.isEnabled();
    }

    public void disableFeature(String featureId)
    {
        Feature<?> feature = features.getFeature(featureId);
        feature.disable();
        store.store(featureId, feature);
    }

    public void enableFeature(String featureId)
    {
        Feature<?> feature = features.getFeature(featureId);
        feature.enable();
        store.store(featureId, feature);
    }
}
