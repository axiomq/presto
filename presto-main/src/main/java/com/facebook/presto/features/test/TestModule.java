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
package com.facebook.presto.features.test;

import com.facebook.airlift.configuration.AbstractConfigurationAwareModule;
import com.facebook.presto.features.Feature;
import com.google.inject.Binder;

import static com.facebook.airlift.configuration.ConfigBinder.configBinder;
import static com.facebook.presto.features.test.FeatureToggleBinder.featureToggleBinder;

public class TestModule
        extends AbstractConfigurationAwareModule
{
    private final Feature<Feature01> feature;

    public TestModule(Feature<Feature01> feature)
    {
        this.feature = feature;
    }

    @Override
    protected void setup(Binder binder)
    {
        configBinder(binder).bindConfig(Feature01Config.class);
        featureToggleBinder(binder).bind(feature.getConfiguration().getFeatureClass(), feature);
    }
}
