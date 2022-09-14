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
package com.facebook.presto.features.test;

import com.facebook.presto.features.Feature;
import com.facebook.presto.features.FeatureConfiguration;
import com.facebook.presto.features.FeatureInstance;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main
{
    public static void main(String[] args)
    {
        String feature01Impl02ID = "feature01Impl02";
        String feature01Impl01ID = "feature01Impl01";

        Feature01Impl01 feature01Impl01 = new Feature01Impl01();
        Feature01Impl02 feature01Impl02 = new Feature01Impl02();
        FeatureInstance<Feature01> feature01FeatureInstance = new FeatureInstance<>(feature01Impl01, feature01Impl01ID, false);
        FeatureInstance<Feature01> feature02FeatureInstance = new FeatureInstance<>(feature01Impl02, feature01Impl02ID, true);

        FeatureConfiguration<Feature01> configuration = new FeatureConfiguration<>(Feature01.class, "NONE", true);
        Feature<Feature01> feature1 = new Feature<>("feature01", "desc", configuration);
        feature1.addInstance(feature01FeatureInstance);
        feature1.addDefaultInstance(feature02FeatureInstance);

        TestModule testModule = new TestModule(feature1);
        Injector guice = Guice.createInjector(testModule);
/*
//        Binder binder = testModule.getBinder();
//        FeatureToggleBinder.featureToggleBinder(binder).bind(Feature01.class, feature1);
        TestProviderService service = guice.getInstance(TestProviderService.class);
        service.test();

        feature1.changeDefaultInstance(feature01Impl01ID);
        service.test();
*/

//        TestService testService = guice.getInstance(TestService.class);
//        testService.test();
//        testService.test();

    }
}
