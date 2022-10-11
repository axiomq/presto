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

import com.facebook.airlift.configuration.AbstractConfigurationAwareModule;
import com.facebook.airlift.log.Logger;
import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;

import java.util.Map;

import static com.facebook.presto.features.config.Util.classForName;

public class FeatureToggleModule
        extends AbstractConfigurationAwareModule
{
    private static final Logger log = Logger.get(FeatureToggleModule.class);

    @Override
    protected void setup(Binder binder)
    {
        FeatureToggleConfig config = buildConfigObject(FeatureToggleConfig.class);
        Map<String, FeatureConfiguration> map = ConfigurationParser.parseConfiguration(config);
        MapBinder<String, Object> featureMapBinder = MapBinder.newMapBinder(binder, String.class, Object.class);
        map.forEach((featureClassName, configuration) ->
        {
            configuration.getFeatureInstances().forEach(instanceClassName ->
            {
                featureMapBinder.addBinding(instanceClassName).to(classForName(instanceClassName));
            });
//            Class featureClass = classForName(featureClassName);
//            binder.bind(featureClass).toProvider(() -> binder.getProvider(FeatureToggle.class).get().getCurrentInstance(featureClassName));
        });
    }
}
