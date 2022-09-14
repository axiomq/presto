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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.multibindings.MapBinder;

import java.util.Map;

public class FeatureProvider
{
    private final Map<String, Object> mapBinder;
    private final FeatureToggle featureToggle;

    @Inject
    public FeatureProvider(Map<String, Object> mapBinder, FeatureToggle featureToggle)
    {
        this.mapBinder = mapBinder;
        this.featureToggle = featureToggle;
    }

    public Object getInstance(String clazz)
    {
        return mapBinder.get(clazz);
    }

    private void install()
    {
//        Guice.createInjector(binder -> featureConfigurationMap.forEach((featureId, configuration) -> {
//            MapBinder<String, Object> mapBinder = MapBinder.newMapBinder(binder, String.class, Object.class);
//            String className = configuration.getFeatureClass();
//            Class clazz = classForName(className);
//            binder.bind(clazz);
//            mapBinder.addBinding(className).to(clazz);
//            Feature<?> feature = new Feature<>(featureId, configuration);
//            binder.bind(clazz).toProvider(feature::getCurrentInstance);
//        }));
    }
}
