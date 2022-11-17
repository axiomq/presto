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
package com.facebook.presto.features.binder;

import com.facebook.presto.features.annotations.FeatureToggle;
import com.facebook.presto.features.classes.Feature01;
import com.facebook.presto.features.classes.Feature01Impl01;
import com.facebook.presto.features.classes.Feature01Impl02;
import com.facebook.presto.features.config.FeatureConfiguration;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.facebook.presto.features.binder.FeatureToggleBinder.featureToggleBinder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FeatureToggleTests
{
    private final Map<String, FeatureConfiguration> map = new HashMap<>();

    private static void sleep()
    {
        try {
            Thread.sleep(6000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHotReload()
    {
        map.clear();

        class Runner
        {
            private final Provider<Feature01> feature01;

            @Inject
            public Runner(
                    @FeatureToggle("feature01") Provider<Feature01> feature01)
            {
                this.feature01 = feature01;
            }

            public String testFeature01()
            {
                return feature01.get().test();
            }
        }

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder, Feature01.class)
                        .featureId("feature01")
                        .baseClass(Feature01.class)
                        .defaultClass(Feature01Impl01.class)
                        .allOf(Feature01Impl01.class, Feature01Impl02.class)
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<Runner> runner = injector.getProvider(Runner.class);

        // fallback to default instance
        String className = runner.get().testFeature01();
        assertEquals("Feature01Impl01", className);

        // change configuration
        map.put("feature01", FeatureConfiguration.builder().featureId("feature01").currentInstance(Feature01Impl02.class).build());
        sleep();

        className = runner.get().testFeature01();
        assertEquals("Feature01Impl02", className);
    }

    @Test
    public void testProviderInjection()
    {
        map.clear();

        class Runner
        {
            private final Provider<Feature01> feature01;

            @Inject
            public Runner(
                    @FeatureToggle("feature01") Provider<Feature01> feature01)
            {
                this.feature01 = feature01;
            }

            public String testFeature01()
            {
                return feature01.get().test();
            }
        }

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder, Feature01.class)
                        .featureId("feature01")
                        .baseClass(Feature01.class)
                        .defaultClass(Feature01Impl01.class)
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<Runner> runner = injector.getProvider(Runner.class);

        String className = runner.get().testFeature01();
        assertEquals("Feature01Impl01", className);
    }

    @Test
    public void testSupplierInjection()
    {
        map.clear();
        class Runner
        {
            private final Supplier<Boolean> isFeature03Enabled;

            @Inject
            public Runner(@FeatureToggle("feature03") Supplier<Boolean> isFeature03Enabled)
            {
                this.isFeature03Enabled = isFeature03Enabled;
            }

            public boolean testFeature03Enabled()
            {
                return isFeature03Enabled.get();
            }
        }

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .featureId("feature03")
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<Runner> runner = injector.getProvider(Runner.class);

        boolean enabled = runner.get().testFeature03Enabled();
        assertTrue(enabled);

        // change configuration
        map.put("feature03", FeatureConfiguration.builder().enabled(false).build());
        sleep();

        enabled = runner.get().testFeature03Enabled();
        assertFalse(enabled);
    }

    @Test
    public void testFunctionInjection()
    {
        map.clear();
        class Runner
        {
            private final Function<Object, Boolean> isFeature04Enabled;

            @Inject
            public Runner(@FeatureToggle("feature04") Function<Object, Boolean> isFeature04Enabled)
            {
                this.isFeature04Enabled = isFeature04Enabled;
            }

            public boolean testFeature04Enabled()
            {
                return isFeature04Enabled.apply("string");
            }
        }

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .featureId("feature04")
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<Runner> runner = injector.getProvider(Runner.class);

        boolean enabled = runner.get().testFeature04Enabled();
        assertTrue(enabled);

        // change configuration
        map.put("feature04", FeatureConfiguration.builder().enabled(false).build());
        sleep();

        enabled = runner.get().testFeature04Enabled();
        assertFalse(enabled);
    }

    @Test
    public void testFunctionInjectionWithStrategy()
    {
        map.clear();
        class Runner
        {
            private final Function<Object, Boolean> isFeature04Enabled;

            @Inject
            public Runner(@FeatureToggle("feature04") Function<Object, Boolean> isFeature04Enabled)
            {
                this.isFeature04Enabled = isFeature04Enabled;
            }

            public boolean testFeature04Enabled()
            {
                return isFeature04Enabled.apply("string");
            }
        }

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .featureId("feature04")
                        .toggleStrategy("AllowAll")
                        .toggleStrategyConfig(ImmutableMap.of("key", "value", "key2", "value2"))
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<Runner> runner = injector.getProvider(Runner.class);

        boolean enabled = runner.get().testFeature04Enabled();
        assertTrue(enabled);

        // change configuration
        map.put("feature04", FeatureConfiguration.builder().enabled(false).build());
        sleep();

        enabled = runner.get().testFeature04Enabled();
        assertFalse(enabled);
    }
}
