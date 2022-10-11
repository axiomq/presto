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

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Map;
import java.util.Optional;

public class FeatureProvider
{
    @Inject
    private Map<String, Object> featureMapBinder;
    @Inject
    private FeatureToggle featureToggle;
//
//    @Inject
//    public FeatureProvider(Map<String, Object> featureMapBinder, FeatureToggle featureToggle)
//    {
//        this.featureMapBinder = featureMapBinder;
//        this.featureToggle = featureToggle;
//    }

    public Provider<Object> getProvider(String featureClass)
    {
        return () -> {
            Optional<FeatureConfiguration> featureConfiguration = featureToggle.getFeatureConfigurations().stream()
                    .filter(fc -> featureClass.equals(fc.getFeatureClass())).findFirst();
            return featureConfiguration.map(configuration -> featureMapBinder.get(configuration.getCurrentInstance())).orElse(null);
        };
    }
}
