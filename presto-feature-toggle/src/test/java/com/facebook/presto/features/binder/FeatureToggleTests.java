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
import com.facebook.presto.features.classes.HotReloadFeature;
import com.facebook.presto.features.classes.HotReloadFeatureImpl01;
import com.facebook.presto.features.classes.HotReloadFeatureImpl02;
import com.facebook.presto.features.classes.ProviderFeature;
import com.facebook.presto.features.classes.ProviderFeatureImpl;
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

import static com.facebook.presto.features.TestUtils.sleep;
import static com.facebook.presto.features.binder.FeatureToggleBinder.featureToggleBinder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FeatureToggleTests
{
    private final Map<String, FeatureConfiguration> map = new HashMap<>();

    /**
     * test hot swapping of implementations of the base interface {@link HotReloadFeature}
     * <p>
     * definition of hot reloadable (hot swappable) feature toggle with id  "HotReloadFeature"
     * <pre>{@code
     *  binder -> featureToggleBinder(binder, HotReloadFeature.class) // creates new feature toggle
     *               .featureId("HotReloadFeature")              // sets feature id
     *               .baseClass(HotReloadFeature.class)          // defines base interface of the feature
     *               .defaultClass(HotReloadFeatureImpl01.class) // defines default implementation for the feature
     *               // defines list of implementations of the base interface that can be hot swapped on runtime
     *               .allOf(HotReloadFeatureImpl01.class, HotReloadFeatureImpl02.class)
     *              .bind()
     * }</pre>
     * <p>
     * Feature Toggles injects default implementation of the HotReloadFeature interface to provider annotated with @FeatureToggle("HotReloadFeature")
     * <pre>{@code
     *         @Inject
     *         public HotReloadRunner(@FeatureToggle("HotReloadFeature") Provider<HotReloadFeature> hotReloadFeature)
     *         {
     *             this.hotReloadFeature = hotReloadFeature;
     *         }
     * }</pre>
     * <p>
     * in first test default implementation is provided in HotReloadRunner instance
     * then we change run time configuration for feature, changing parameter currentInstance to HotReloadFeatureImpl02.class
     * <pre>{@code
     *     map.put("HotReloadFeature", FeatureConfiguration.builder().featureId("HotReloadFeature").currentInstance(HotReloadFeatureImpl02.class).build());
     * }</pre>
     * it is same as changing configuration param feature.{featureId}.currentInstance
     * <pre>
     *     feature.HotReloadFeature.currentInstance=com.facebook.presto.features.classes.HotReloadFeatureImpl02
     * </pre>
     * after configuration refresh period new implementation is provided in HotReloadRunner instance
     */
    @Test
    public void testHotReload()
    {
        map.clear();

        Injector injector = Guice.createInjector(
                // bind Feature Toggle module
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder, HotReloadFeature.class)
                        .featureId("HotReloadFeature")                          // sets feature id
                        .baseClass(HotReloadFeature.class)                      // defines base interface of the feature
                        .defaultClass(HotReloadFeatureImpl01.class)             // defines default implementation for the feature
                        // defines list of implementations of the base interface that can be hot swapped on runtime
                        .allOf(HotReloadFeatureImpl01.class, HotReloadFeatureImpl02.class)
                        .bind(),
                // bind Feature Toggle run-time configuration
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<HotReloadRunner> runner = injector.getProvider(HotReloadRunner.class);

        // fallback to default instance
        String className = runner.get().testHotReloadFeature();
        assertEquals("HotReloadFeatureImpl01", className);

        // change configuration
        map.put("HotReloadFeature", FeatureConfiguration.builder().featureId("HotReloadFeature").currentInstance(HotReloadFeatureImpl02.class).build());
        // wait for configuration to reload
        sleep();

        className = runner.get().testHotReloadFeature();
        assertEquals("HotReloadFeatureImpl02", className);
    }

    @Test
    public void testProviderInjection()
    {
        map.clear();

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder, ProviderFeature.class)
                        .featureId("ProviderFeature")
                        .baseClass(ProviderFeature.class)
                        .defaultClass(ProviderFeatureImpl.class)
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<ProviderInjectionRunner> runner = injector.getProvider(ProviderInjectionRunner.class);

        String className = runner.get().testProviderFeature();
        assertEquals("ProviderFeatureImpl", className);
    }

    @Test
    public void testSupplierInjection()
    {
        map.clear();

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .featureId("SimpleFeature")
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<SupplierInjectionRunner> runner = injector.getProvider(SupplierInjectionRunner.class);

        boolean enabled = runner.get().testSimpleFeatureEnabled();
        assertTrue(enabled);

        // change configuration and wait 6 seconds to feature toggle reloads configuration
        map.put("SimpleFeature", FeatureConfiguration.builder().enabled(false).build());
        sleep();

        enabled = runner.get().testSimpleFeatureEnabled();
        assertFalse(enabled);
    }

    @Test
    public void testFunctionInjection()
    {
        map.clear();

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .featureId("FunctionInjectionFeature")
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<FunctionInjectionRunner> runner = injector.getProvider(FunctionInjectionRunner.class);

        boolean enabled = runner.get().testFunctionInjectionFeatureEnabled();
        assertTrue(enabled);

        // change configuration
        map.put("FunctionInjectionFeature", FeatureConfiguration.builder().enabled(false).build());
        sleep();

        enabled = runner.get().testFunctionInjectionFeatureEnabled();
        assertFalse(enabled);
    }

    @Test
    public void testFunctionInjectionWithStrategy()
    {
        map.clear();

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .featureId("FunctionInjectionWithStrategy")
                        .toggleStrategy("AllowAll")
                        .toggleStrategyConfig(ImmutableMap.of("key", "value", "key2", "value2"))
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<FunctionInjectionWithStrategyRunner> runner = injector.getProvider(FunctionInjectionWithStrategyRunner.class);

        boolean enabled = runner.get().testFunctionInjectionWithStrategyEnabled();
        assertTrue(enabled);

        // change configuration
        map.put("FunctionInjectionWithStrategy", FeatureConfiguration.builder().enabled(false).build());
        sleep();

        enabled = runner.get().testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
    }

    private static class HotReloadRunner
    {
        private final Provider<HotReloadFeature> hotReloadFeature;

        @Inject
        public HotReloadRunner(
                @FeatureToggle("HotReloadFeature") Provider<HotReloadFeature> hotReloadFeature)
        {
            this.hotReloadFeature = hotReloadFeature;
        }

        public String testHotReloadFeature()
        {
            return hotReloadFeature.get().test();
        }
    }

    private static class ProviderInjectionRunner
    {
        private final Provider<ProviderFeature> providerFeature;

        @Inject
        public ProviderInjectionRunner(
                @FeatureToggle("ProviderFeature") Provider<ProviderFeature> providerFeature)
        {
            this.providerFeature = providerFeature;
        }

        public String testProviderFeature()
        {
            return providerFeature.get().test();
        }
    }

    private static class SupplierInjectionRunner
    {
        private final Supplier<Boolean> isSimpleFeatureEnabled;

        @Inject
        public SupplierInjectionRunner(@FeatureToggle("SimpleFeature") Supplier<Boolean> isSimpleFeatureEnabled)
        {
            this.isSimpleFeatureEnabled = isSimpleFeatureEnabled;
        }

        public boolean testSimpleFeatureEnabled()
        {
            return isSimpleFeatureEnabled.get();
        }
    }

    private static class FunctionInjectionRunner
    {
        private final Function<Object, Boolean> isFunctionInjectionFeatureEnabled;

        @Inject
        public FunctionInjectionRunner(@FeatureToggle("FunctionInjectionFeature") Function<Object, Boolean> isFunctionInjectionFeatureEnabled)
        {
            this.isFunctionInjectionFeatureEnabled = isFunctionInjectionFeatureEnabled;
        }

        public boolean testFunctionInjectionFeatureEnabled()
        {
            return isFunctionInjectionFeatureEnabled.apply("string");
        }
    }

    private static class FunctionInjectionWithStrategyRunner
    {
        private final Function<Object, Boolean> isFunctionInjectionWithStrategyEnabled;

        @Inject
        public FunctionInjectionWithStrategyRunner(@FeatureToggle("FunctionInjectionWithStrategy") Function<Object, Boolean> isFunctionInjectionWithStrategyEnabled)
        {
            this.isFunctionInjectionWithStrategyEnabled = isFunctionInjectionWithStrategyEnabled;
        }

        public boolean testFunctionInjectionWithStrategyEnabled()
        {
            return isFunctionInjectionWithStrategyEnabled.apply("string");
        }
    }
}
