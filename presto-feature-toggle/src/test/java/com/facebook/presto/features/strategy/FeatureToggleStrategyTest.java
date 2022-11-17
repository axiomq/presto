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
package com.facebook.presto.features.strategy;

import com.facebook.presto.features.annotations.FeatureToggle;
import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.binder.TestFeatureToggleModule;
import com.facebook.presto.features.config.FeatureConfiguration;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.facebook.presto.features.TestUtils.sleep;
import static com.facebook.presto.features.binder.FeatureToggleBinder.featureToggleBinder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FeatureToggleStrategyTest
{
    private final Map<String, FeatureConfiguration> map = new HashMap<>();

    @Test
    public void testRegisterStrategy()
    {
        AtomicReference<String> allowedReference = new AtomicReference<>("");
        map.clear();
        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .registerToggleStrategy("BooleanStringStrategy", BooleanStringStrategy.class)
                        .featureId("FunctionInjectionWithStrategy")
                        .toggleStrategy("BooleanStringStrategy")
                        .toggleStrategyConfig(ImmutableMap.of("allow-values", "yes,no"))
                        .bind(),
                binder -> binder.bind(String.class).annotatedWith(Names.named("allowed")).toProvider(allowedReference::get),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        FunctionInjectionWithBooleanStrategyRunner runner = injector.getProvider(FunctionInjectionWithBooleanStrategyRunner.class).get();

        boolean enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);

        allowedReference.set("yes");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertTrue(enabled);
        allowedReference.set("no");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
        allowedReference.set("not sure");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
        allowedReference.set(null);
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);

        // change configuration
        FeatureToggleStrategyConfig featureToggleStrategyConfig = new FeatureToggleStrategyConfig("BooleanStringStrategy", ImmutableMap.of("allow-values", "true,false"));
        map.put("FunctionInjectionWithStrategy", FeatureConfiguration.builder().featureToggleStrategyConfig(featureToggleStrategyConfig).build());
        sleep();

        allowedReference.set("yes");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
        allowedReference.set("true");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertTrue(enabled);
        allowedReference.set("false");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
        allowedReference.set("not sure");
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
        allowedReference.set(null);
        enabled = runner.testFunctionInjectionWithStrategyEnabled();
        assertFalse(enabled);
    }

    private static class FunctionInjectionWithBooleanStrategyRunner
    {
        private final Function<Object, Boolean> isFunctionInjectionWithStrategyEnabled;
        private final Provider<String> allowed;

        @Inject
        public FunctionInjectionWithBooleanStrategyRunner(
                @FeatureToggle("FunctionInjectionWithStrategy") Function<Object, Boolean> isFunctionInjectionWithStrategyEnabled,
                @Named("allowed") Provider<String> allowed)
        {
            this.isFunctionInjectionWithStrategyEnabled = isFunctionInjectionWithStrategyEnabled;
            this.allowed = allowed;
        }

        public boolean testFunctionInjectionWithStrategyEnabled()
        {
            return isFunctionInjectionWithStrategyEnabled.apply(allowed.get());
        }
    }
}
