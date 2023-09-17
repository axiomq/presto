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
import com.facebook.presto.features.config.FeatureToggleConfig;
import com.facebook.presto.features.config.FeatureToggleConfigurationManager;
import com.facebook.presto.features.config.FeatureToggleModule;
import com.facebook.presto.features.test.TestConfigurationSource;
import com.facebook.presto.features.test.TestConfigurationSourceFactory;
import com.facebook.presto.spi.features.FeatureConfiguration;
import com.facebook.presto.spi.features.FeatureToggleStrategyConfig;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.airlift.units.Duration;
import org.testng.annotations.BeforeMethod;
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
    /**
     * map is configuration provider for dynamic configuration parameters
     */
    private final Map<String, FeatureConfiguration> config = new HashMap<>();
    private TestConfigurationSourceFactory configurationSourceFactory;
    private FeatureToggleConfig featureToggleConfig;

    @BeforeMethod
    public void prepare()
    {
        config.clear();
        featureToggleConfig = new FeatureToggleConfig();
        featureToggleConfig.setRefreshPeriod(Duration.valueOf("2s"));
        featureToggleConfig.setConfigSourceType("test");
        configurationSourceFactory = new TestConfigurationSourceFactory(new TestConfigurationSource(config));
    }

    /**
     * test injection function that accepts object as parameter.
     * <p>
     * Function is used to evaluate string in BooleanStringStrategy feature toggle strategy
     * <p>
     * definition of the feature toggle with id "FunctionInjectionWithStrategy".
     * BooleanStringStrategy will evaluate input string to enable or disable this Feature
     * <pre>{@code
     *      binder -> featureToggleBinder(binder)
     *                         .registerToggleStrategy("BooleanStringStrategy", BooleanStringStrategy.class)
     *                         .featureId("FunctionInjectionWithStrategy")
     *                         .toggleStrategy("BooleanStringStrategy")
     *                         .toggleStrategyConfig(ImmutableMap.of("allow-values", "yes,no"))
     *                         .bind(),
     * }</pre>
     * <p>
     * Feature Toggles injects function to parameter annotated with @FeatureToggle("FunctionInjectionWithStrategy")
     * <pre>{@code
     *         @Inject
     *         public FunctionInjectionRunner(@FeatureToggle("FunctionInjectionFeature") Function<Object, Boolean> isFunctionInjectionFeatureEnabled)
     *         {
     *            this.isFunctionInjectionFeatureEnabled = isFunctionInjectionFeatureEnabled;
     *         }
     * }</pre>
     * <p>
     * Next, we will define binding of string provider to simulate dynamic change of value to be evaluated
     * <pre>{@code
     *          binder -> binder.bind(String.class).annotatedWith(Names.named("allowed")).toProvider(allowedReference::get)
     * }</pre>
     * allowed reference is container of the string value
     * <pre>{@code
     *          AtomicReference<String> allowedReference = new AtomicReference<>("");
     * }</pre>
     * <p>
     * in first set of tests feature with id "BooleanStringStrategy" accepts "yes,no" values,
     * if input param is yes strategy will evaluate this as true, in other cases will evaluate as false
     * <pre>{@code
     *      isFunctionInjectionWithStrategyEnabled.apply("yes") will return true
     * }</pre>
     * then we change run time configuration for feature, changing parameter "allow-values" to "true,false"
     * <pre>{@code
     *     FeatureToggleStrategyConfig featureToggleStrategyConfig = new FeatureToggleStrategyConfig("BooleanStringStrategy", ImmutableMap.of("allow-values", "true,false"));
     *     map.put("FunctionInjectionWithStrategy", FeatureConfiguration.builder().featureToggleStrategyConfig(featureToggleStrategyConfig).build());
     * }</pre>
     * it is same as changing configuration param feature.{FunctionInjectionWithStrategy}.strategy.allow-values=yes,no
     * <pre>{@code
     *    feature.FunctionInjectionWithStrategy.strategy.allow-values=yes,no
     * }</pre>
     * after configuration change if input param is "true" strategy will evaluate this as true, in other cases will evaluate as false
     * <pre>{@code
     *           isFunctionInjectionWithStrategyEnabled.apply("true") // will return true
     *           isFunctionInjectionWithStrategyEnabled.apply("yes") // will return false
     *  }</pre>
     */
    @Test
    public void testRegisterStrategy()
    {
        config.clear();
        AtomicReference<String> allowedReference = new AtomicReference<>("");
        Injector injector = Guice.createInjector(
                // bind Feature Toggle config
                binder -> binder.bind(FeatureToggleConfig.class).toInstance(featureToggleConfig),
                // bind Feature Toggle module
                new FeatureToggleModule(),
                binder -> featureToggleBinder(binder)
                        .registerToggleStrategy("BooleanStringStrategy", BooleanStringStrategy.class)
                        .featureId("FunctionInjectionWithStrategy")
                        .toggleStrategy("BooleanStringStrategy")
                        .toggleStrategyConfig(ImmutableMap.of("allow-values", "yes,no"))
                        .bind(),
                binder -> binder.bind(String.class).annotatedWith(Names.named("allowed")).toProvider(allowedReference::get));
        injector.getProvider(PrestoFeatureToggle.class).get();
        // configuration sources factories are added through plugin mechanism
        injector.getInstance(FeatureToggleConfigurationManager.class).addConfigurationSourceFactory(configurationSourceFactory);
        //  load configuration source
        injector.getInstance(FeatureToggleConfigurationManager.class).loadConfigurationSources(ImmutableMap.of(TestConfigurationSource.NAME, ImmutableMap.of(FeatureToggleConfig.FEATURES_CONFIG_SOURCE_TYPE, TestConfigurationSource.NAME)));

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
        config.put("FunctionInjectionWithStrategy", FeatureConfiguration.builder().featureToggleStrategyConfig(featureToggleStrategyConfig).build());
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
