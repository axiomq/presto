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

public abstract class ForwardingFeatureToggle
        implements FeatureToggle
{
    public static ForwardingFeatureToggle of(Supplier<FeatureToggle> featureToggleControl)
    {
        requireNonNull(featureToggleControl, "featureToggleControl is null");
        return new ForwardingFeatureToggle()
        {
            @Override
            protected FeatureToggle delegate()
            {
                return featureToggleControl.get();
            }
        };
    }

    protected abstract FeatureToggle delegate();

    @Override
    public boolean check(String featureId)
    {
        return delegate().check(featureId);
    }

    @Override
    public String getCurrentInstance(String featureClassName)
    {
        return delegate().getCurrentInstance(featureClassName);
    }

    @Override
    public String getDefaultInstance(String featureClassName)
    {
        return delegate().getDefaultInstance(featureClassName);
    }

    @Override
    public boolean check(String featureId, Object object)
    {
        return delegate().check(featureId, object);
    }

    @Override
    public FeatureConfiguration getFeatureConfiguration(String featureId)
    {
        return delegate().getFeatureConfiguration(featureId);
    }
}
