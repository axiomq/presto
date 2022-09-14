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
package com.facebook.presto.features.obsolete.test;

import com.facebook.presto.features.DefaultFeatures;
import com.facebook.presto.features.Feature;
import com.facebook.presto.features.FeatureConfiguration;
import com.facebook.presto.features.FeatureInstance;
import com.facebook.presto.features.Features;
import com.google.inject.Inject;

import java.util.HashSet;
import java.util.Set;

public class FeatureRegistry
{
    private static final String FEATURE_ID = "featureId";
    private static final String FEATURE_DESCRIPTION = "featureDescription";

    private Set<Feature01> instances = new HashSet<>();

    public void register(
            Features features,
            Class<Feature01> klass,
            Class<? extends Feature01> defaultInstance)
    {
        FeatureConfiguration<Feature01> configuration = new FeatureConfiguration<>(klass, "NONE", true);
        Feature<Feature01> feature = new Feature<>(FEATURE_ID, FEATURE_DESCRIPTION, configuration);
        ((DefaultFeatures) features).addFeature(feature);
        instances.forEach(i -> {
            FeatureInstance<Feature01> instance;
            if (i.getClass() == defaultInstance) {
                instance = new FeatureInstance<>(i, i.getClass().getName(), true);
            }
            else {
                instance = new FeatureInstance<>(i, i.getClass().getName(), false);
            }
            feature.addInstance(instance);
        });
    }

    @Inject
    public void setInstances(Set<Feature01> instances)
    {
        this.instances = instances;
    }
}
