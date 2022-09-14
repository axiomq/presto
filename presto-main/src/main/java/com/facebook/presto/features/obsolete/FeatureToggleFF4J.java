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
package com.facebook.presto.features.obsolete;

import com.facebook.presto.features.Features;
import org.ff4j.FF4j;
import org.ff4j.audit.repository.InMemoryEventRepository;
import org.ff4j.core.Feature;
import org.ff4j.property.store.InMemoryPropertyStore;
import org.ff4j.store.InMemoryFeatureStore;

public class FeatureToggleFF4J
{
    private final FF4j ff4j;

    private Features features;

    public FeatureToggleFF4J(FF4j ff4j)
    {
        this.ff4j = ff4j;
    }

    public static void main(String[] args)
    {
        FF4j ff4j = new FF4j();

        /*
         * Implementation of each store. Here this is boiler plate as if nothing
         * is specified the inmemory is used. Those are really the one that will
         * change depending on your technology.
         */
        InMemoryFeatureStore featureStore = new InMemoryFeatureStore();
        InMemoryPropertyStore propertiesStore = new InMemoryPropertyStore();
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();

        featureStore.create(new Feature("feature", true, "feature description"));

        ff4j.setFeatureStore(featureStore);
        ff4j.setPropertiesStore(propertiesStore);
        ff4j.setEventRepository(eventRepository);

        // Enabling audit and monitoring, default value is false
        ff4j.audit(true);

        // When evaluting not existing features, ff4j will create then but disabled
        ff4j.autoCreate(true);

        boolean check = ff4j.check("feature");
    }
}
