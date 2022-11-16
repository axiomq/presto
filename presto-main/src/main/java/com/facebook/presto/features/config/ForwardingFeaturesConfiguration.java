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

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public abstract class ForwardingFeaturesConfiguration
        implements FeatureToggleConfiguration
{
    public static ForwardingFeaturesConfiguration of(Supplier<FeatureToggleConfiguration> featureToggleConfiguration)
    {
        requireNonNull(featureToggleConfiguration, "featureToggleControl is null");
        return new ForwardingFeaturesConfiguration()
        {
            @Override
            protected FeatureToggleConfiguration delegate()
            {
                return featureToggleConfiguration.get();
            }
        };
    }

    protected abstract FeatureToggleConfiguration delegate();

    @Override
    public String getCurrentInstance(String featureId)
    {
        return delegate().getCurrentInstance(featureId);
    }

    @Override
    public FeatureConfiguration getFeatureConfiguration(String featureId)
    {
        return delegate().getFeatureConfiguration(featureId);
    }
}
