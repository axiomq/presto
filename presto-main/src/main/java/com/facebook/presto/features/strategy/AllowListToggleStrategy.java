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
import com.facebook.presto.SessionRepresentation;
import com.facebook.presto.execution.QueryManager;
import com.facebook.presto.features.config.FeatureConfiguration;
import com.facebook.presto.server.protocol.ExecutingStatementResource;
import com.facebook.presto.spi.QueryId;
import com.google.inject.Inject;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class AllowListToggleStrategy
        implements FeatureToggleStrategy
{
    private static final Logger log = Logger.get(ExecutingStatementResource.class);

    private static final String ALLOW_LIST_SOURCE = "allow-list-source";
    private static final String ALLOW_LIST_USER = "allow-list-user";

    private final QueryManager queryManager;

    @Inject
    public AllowListToggleStrategy(QueryManager queryManager)
    {
        this.queryManager = queryManager;
    }

    @Override
    public boolean check(FeatureConfiguration featureToggleConfiguration, String featureId, Object object)
    {
        log.info("checking feature enabled using AllowListToggleStrategy");
        if (!featureToggleConfiguration.getFeatureToggleStrategyConfig().isPresent()) {
            return true;
        }
        FeatureToggleStrategyConfig featureToggleStrategyConfig = featureToggleConfiguration.getFeatureToggleStrategyConfig().get();
        if (!featureToggleStrategyConfig.active()) {
            return true;
        }
        SessionRepresentation session = queryManager.getQueryInfo((QueryId) object).getSession();
        String user = session.getUser();
        Optional<String> source = session.getSource();
        Optional<String> userPattern = featureToggleStrategyConfig.get(ALLOW_LIST_USER);
        Optional<String> sourcePattern = featureToggleStrategyConfig.get(ALLOW_LIST_SOURCE);
        log.info(format("AllowListToggleStrategy: user %s , source %s, userPattern %s, sourcePattern %s", user, source.orElse("null"), userPattern.orElse("no user pattern"), sourcePattern.orElse("no source pattern")));
        AtomicBoolean allow = new AtomicBoolean(false);
        userPattern.ifPresent(p ->
                allow.set(allow.get() || Pattern.compile(p).matcher(user).matches()));
        sourcePattern.ifPresent(p ->
                source.ifPresent(s ->
                        allow.set(allow.get() || Pattern.compile(p).matcher(source.get()).matches())));
        return allow.get();
    }
}
