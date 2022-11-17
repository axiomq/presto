package com.facebook.presto.features.config;

import com.facebook.presto.features.annotations.FeatureToggle;
import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.facebook.presto.features.classes.Feature01;
import com.facebook.presto.features.classes.Feature01Impl01;
import com.facebook.presto.features.classes.Feature01Impl02;
import com.facebook.presto.features.classes.Feature02;
import com.facebook.presto.features.classes.Feature02Impl01;
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

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
    public void testProviderInjection()
    {
        map.clear();

        Injector injector = Guice.createInjector(
                new TestFeatureToggleModule(),
                binder -> featureToggleBinder(binder, Feature01.class)
                        .featureId("feature01")
                        .baseClass(Feature01.class)
                        .defaultClass(Feature01Impl01.class)
                        .allOf(Feature01Impl01.class, Feature01Impl02.class)
                        .bind(),
                binder -> featureToggleBinder(binder, Feature02.class)
                        .featureId("feature02")
                        .defaultClass(Feature02Impl01.class)
                        .bind(),
                binder -> featureToggleBinder(binder)
                        .featureId("feature03")
                        .bind(),
                binder -> featureToggleBinder(binder)
                        .featureId("feature04")
                        .bind(),
                binder -> binder.bind(new TypeLiteral<Map<String, FeatureConfiguration>>() {}).toInstance(map));
        injector.getProvider(PrestoFeatureToggle.class).get();
        Provider<PrestoRunner> runner = injector.getProvider(PrestoRunner.class);

        // fallback to default instance
        String className = runner.get().testFeature01();
        assertEquals("Feature01Impl01", className);

        className = runner.get().testFeature02();
        assertEquals("Feature02Impl01", className);

        boolean enabled = runner.get().testFeature03Enabled();
        assertTrue(enabled);

        enabled = runner.get().testFeature04Enabled();
        assertTrue(enabled);

        // change configuration
        map.put("feature01", FeatureConfiguration.builder().featureId("feature01").currentInstance(Feature01Impl02.class).build());
        map.put("feature03", FeatureConfiguration.builder().enabled(false).build());
        map.put("feature04", FeatureConfiguration.builder().enabled(false).build());
        sleep();
        className = runner.get().testFeature01();
        assertEquals("Feature01Impl02", className);

        enabled = runner.get().testFeature03Enabled();
        assertFalse(enabled);

        enabled = runner.get().testFeature04Enabled();
        assertFalse(enabled);
    }

    private static class PrestoRunner
    {
        private final Provider<Feature01> feature01;
        private final Feature02 feature02;
        private final Supplier<Boolean> isFeature03Enabled;
        private final Function<Object, Boolean> isFeature04Enabled;

        @Inject
        public PrestoRunner(
                @FeatureToggle("feature01") Provider<Feature01> feature01,
                @FeatureToggle("feature02") Feature02 feature02,
                @FeatureToggle("feature03") Supplier<Boolean> isFeature03Enabled,
                @FeatureToggle("feature04") Function<Object, Boolean> isFeature04Enabled
        )
        {
            this.feature01 = feature01;
            this.feature02 = feature02;
            this.isFeature03Enabled = isFeature03Enabled;
            this.isFeature04Enabled = isFeature04Enabled;
        }

        public String testFeature01()
        {
            return feature01.get().test();
        }

        public String testFeature02()
        {
            return feature02.test();
        }

        public boolean testFeature03Enabled()
        {
            return isFeature03Enabled.get();
        }

        public boolean testFeature04Enabled()
        {
            return isFeature04Enabled.apply("string");
        }
    }
}
