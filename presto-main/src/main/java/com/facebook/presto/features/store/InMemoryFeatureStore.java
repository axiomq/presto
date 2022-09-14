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
package com.facebook.presto.features.store;

import com.facebook.presto.features.Feature;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFeatureStore
        implements FeatureStore
{
    Map<String, Feature<?>> map = new ConcurrentHashMap<>();

    @Override
    public boolean exists(String featureId)
    {
        return map.containsKey(featureId);
    }

    @Override
    public void store(String featureId, Feature<?> feature)
    {
        map.put(featureId, feature);
    }

    @Override
    public Feature<?> read(String featureId)
    {
        return map.get(featureId);
    }
}
