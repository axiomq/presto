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
package com.facebook.presto.features.immutable;

import com.facebook.presto.features.Feature;
import com.google.inject.Binder;

import static java.util.Objects.requireNonNull;

public class FeatureBinder
{
    private final Binder binder;

    private FeatureBinder(Binder binder)
    {
        this.binder = requireNonNull(binder, "binder is null").skipSources(getClass());
    }

    public static FeatureBinder featureToggleBinder(Binder binder)
    {
        return new FeatureBinder(binder);
    }

    public <T> void bind(Class<T> clazz, Feature<T> feature)
    {
        if (feature.isEnabled()) {
            binder.bind(clazz).toProvider(() -> feature.getDefaultInstance().getInstance());
        }
    }
}
