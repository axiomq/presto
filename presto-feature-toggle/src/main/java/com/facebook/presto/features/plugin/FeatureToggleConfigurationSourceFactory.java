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
package com.facebook.presto.features.plugin;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;

import java.util.Map;

import static com.facebook.airlift.concurrent.Threads.daemonThreadsNamed;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class FeatureToggleConfigurationSourceFactory
        implements ConfigurationSourceFactory
{
    @Override
    public String getName()
    {
        return "feature toggle";
    }

    @Override
    public ConfigrationSource create(String catalogName, Map<String, String> requiredConfig, ConnectorContext context)
    {
        ListeningScheduledExecutorService executorService = listeningDecorator(newSingleThreadScheduledExecutor(daemonThreadsNamed("blackhole")));
        return new PrestoFileConfigurationSource(
                new BlackHoleMetadata(),
                new BlackHoleSplitManager(),
                new BlackHolePageSourceProvider(executorService),
                new BlackHolePageSinkProvider(executorService),
                new BlackHoleNodePartitioningProvider(context.getNodeManager()),
                context.getTypeManager(),
                executorService);
    }
}
