package com.facebook.presto.features.config;

import com.facebook.airlift.log.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class OsToggleStrategy
        implements FeatureToggleStrategy
{
    private static final Logger log = Logger.get(OsToggleStrategy.class);

    private static final String OS_NAME = "os_name";
    private static final String OS_VERSION = "os_version";
    private static final String OS_ARCH = "os_arch";

    @Override
    public boolean check(FeatureToggle featureToggle, String featureId)
    {
        FeatureConfiguration configuration = featureToggle.getFeatureConfiguration(featureId);
        FeatureToggleStrategyConfig featureToggleStrategyConfig = configuration.getFeatureToggleStrategyConfig();
        if (!featureToggleStrategyConfig.active()) {
            return true;
        }
        //Operating system name
        String osName = System.getProperty("os.name");
        //Operating system version
        String osVersion = System.getProperty("os.version");
        //Operating system architecture
        String osArch = System.getProperty("os.arch");

        Optional<String> osPattern = featureToggleStrategyConfig.get(OS_NAME);
        Optional<String> versionPattern = featureToggleStrategyConfig.get(OS_VERSION);
        Optional<String> archPattern = featureToggleStrategyConfig.get(OS_ARCH);

        log.info("CHECKING feature %s in %s %s %s with config: name = %s, version %s, arch %s ", featureId, osName, osVersion, osArch, osPattern.orElse("N/A"), versionPattern.orElse("N/A"), archPattern.orElse("N/A"));

        AtomicBoolean allow = new AtomicBoolean(false);
        osPattern.ifPresent(p ->
                allow.set(allow.get() || Pattern.compile(p).matcher(osName).matches()));
        versionPattern.ifPresent(p ->
                allow.set(allow.get() || Pattern.compile(p).matcher(osVersion).matches()));
        archPattern.ifPresent(p ->
                allow.set(allow.get() || Pattern.compile(p).matcher(osArch).matches()));
        return allow.get();
    }
}
