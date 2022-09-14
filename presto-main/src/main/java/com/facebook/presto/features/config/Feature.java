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

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import org.jetbrains.annotations.NotNull;

public class Feature<T>
{
    private final String featureId;
    private final FeatureConfiguration configuration;

    public Feature(String featureId, FeatureConfiguration configuration)
    {
        this.featureId = featureId;
        this.configuration = configuration;
    }

    public String getFeatureId()
    {
        return featureId;
    }

    public FeatureConfiguration getConfiguration()
    {
        return configuration;
    }

    public void config(Binder binder)
    {
            Class<?> clazz = getClazz(configuration.getFeatureClass());
            MapBinder<String, ?> mapBinder = MapBinder.newMapBinder(binder, String.class, clazz);
            configuration.getFeatureInstances().forEach(i -> {
                mapBinder.addBinding(i).to(getClazz(i));
            });
    }

    @NotNull
    private Class getClazz(String configuration)
    {
        try {
            return Class.forName(configuration);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getCurrentInstance() {
        return null;
    }

}
