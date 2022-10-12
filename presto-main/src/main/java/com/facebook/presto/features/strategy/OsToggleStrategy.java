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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.features.config.FeatureConfiguration;

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
    public boolean check(FeatureConfiguration configuration, String featureId)
    {
        Optional<FeatureToggleStrategyConfig> featureToggleStrategyConfigOptional = configuration.getFeatureToggleStrategyConfig();
        if (!featureToggleStrategyConfigOptional.isPresent()) {
            return true;
        }
        FeatureToggleStrategyConfig featureToggleStrategyConfig = featureToggleStrategyConfigOptional.get();
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
