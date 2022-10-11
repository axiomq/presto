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
package com.facebook.presto.features.obsolete.test;

import com.facebook.presto.features.Feature;
import com.facebook.presto.features.FeatureConfiguration;
import com.facebook.presto.features.FeatureInstance;
import com.facebook.presto.features.FeatureToggle;
import com.facebook.presto.features.store.FeatureStore;
import com.facebook.presto.features.store.InMemoryFeatureStore;

public class Main
{
    private Main() {}

    public static void main(String[] args)
    {
        FeatureToggle featureToggle = new FeatureToggle();
        FeatureStore featureStore = new InMemoryFeatureStore();
        featureToggle.setStore(featureStore);
        FeatureConfiguration<String> featureConfiguration = new FeatureConfiguration<>(String.class, "NONE", true);
        String featureId = "stringFeature";
        Feature<String> stringFeature = new Feature<>(featureId, "string feature description", featureConfiguration);
        featureToggle.addFeature(stringFeature);

        boolean enabled = featureToggle.checkFeature(featureId);
        featureToggle.disableFeature(featureId);

        enabled = featureToggle.checkFeature(featureId);
        featureToggle.enableFeature(featureId);

        Feature01Impl01 feature01Impl01 = new Feature01Impl01();
        Feature01Impl02 feature01Impl02 = new Feature01Impl02();
        FeatureInstance<Feature01> feature01FeatureInstance = new FeatureInstance<>(feature01Impl01, "feature01Impl01", false);
        FeatureInstance<Feature01> feature02FeatureInstance = new FeatureInstance<>(feature01Impl02, "feature01Impl01", true);

        FeatureConfiguration<Feature01> configuration = new FeatureConfiguration<>(Feature01.class, "NONE", true);
        Feature<Feature01> feature1 = new Feature<>("feature01", "desc", configuration);
        feature1.addInstance(feature01FeatureInstance);
        feature1.addDefaultInstance(feature02FeatureInstance);

        featureToggle.getFeatures().getFeature("feature01", Feature01.class).test();
    }
/*

    public static void mainz(String[] args)
    {
        json();

        Feature01Impl01 feature01Impl01 = new Feature01Impl01();
        Feature01Impl02 feature01Impl02 = new Feature01Impl02();
        FeatureInstance<Feature01> feature01FeatureInstance = new FeatureInstance<>(feature01Impl01, "feature01Impl01", false);
        FeatureInstance<Feature01> feature02FeatureInstance = new FeatureInstance<>(feature01Impl02, "feature01Impl01", true);

        FeatureConfiguration<Feature01> configuration = new FeatureConfiguration<>("feature01", Feature01.class, true);
        Feature<Feature01> feature1 = new Feature<>("feature01", "desc", configuration);
        feature1.addInstance(feature01FeatureInstance);
        feature1.addDefaultInstance(feature02FeatureInstance);

        DefaultFeatures features = new DefaultFeatures();
        features.addFeature(feature1);

        Feature01 instance = features.getFeature(configuration.getName(), configuration.getFeatureClass());
        boolean eq = Objects.equals(instance, feature01Impl02);
        features.getFeature(configuration.getName(), configuration.getFeatureClass()).test();
        System.out.println();
        boolean featureEnabled = features.checkFeature(configuration.getName());
    }
*/

    static void json()
    {
/*
        Feature feature = new Feature(UUID.randomUUID().toString(), "Feature Description", true, new FeatureConfiguration());

        String json = feature.toJson();
        System.out.println(feature);
        System.out.println(json);

        System.out.println(" - - - ");

        Feature fromJson = Feature.fromJson(json);
        json = feature.toJson();
        System.out.println(feature);
        System.out.println(json);
        Feature01Impl01 feature01Impl01 = new Feature01Impl01();
        Feature01Impl02 feature01Impl02 = new Feature01Impl02();

        FeatureConfiguration<Feature01> configuration = new FeatureConfiguration<>("feature01", Feature01.class, true);

        Feature<Feature01> feature1 = new Feature<>("feature01", "desc", configuration);
        feature1.addInstance(new FeatureInstance<>(feature01Impl01));
        feature1.addDefaultInstance(feature01Impl02);

        json = configuration.toJson();
        System.out.println(configuration);
        System.out.println(json);

        System.out.println(" - - - ");

        FeatureConfiguration<?> fromJsonConfig = FeatureConfiguration.fromJson(json);
        json = fromJsonConfig.toJson();
        System.out.println(fromJsonConfig);
        System.out.println(json);

*/
    }
}
