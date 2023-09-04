# Feature Toggles

Feature Toggles should allow teams to modify system behavior without changing code. Feature Toggles are configured using Google Guice. The basic definition of toggles is created
using FeatureToggleBinder. FeatureToggleBinder creates FeatureToggle, and additional configuration can be done using feature configuration.

## Contents

1. [Configuration](#configuration)
2. [Defining Feature Toggles](#defining-feature-toggles)
    1. [Simple feature toggle definition](#simple-feature-toggle-definition)
    2. [Hot reloadable feature toggle definition](#hot-reloadable-feature-toggle-definition)
    3. [Strategy-based Feature Toggling](#strategy-based-feature-toggling)
3. [Examples](#examples)
    1. [Query Cancel Feature](#query-cancel-feature)
    2. [feature-config.properties file example](#feature-toggle-config-file-example)

## Configuration

Feature Toggle are allowed by default.
Feature toggles can be defined using featureToggleBinder. By default, only static feature toggles are allowed.
Feature toggles configuration are defined using plugin mechanism. Default Feature toggle plugin defines `file` configuration source.

In Presto config properties we can define `feature.config-source-type` property. For each configuration source type, we need to add source configuration file. Default
directory for source configuration files is `etc/feature-toggle`. Directory for source configuration can be changed in Presto config.properties.

Example feature toggle configuration in config.properties file

```
    features.config-source-type=file
    features.configuration-directory=etc/feature-toggle
    features.refresh-period=30s
```

- `configuration-source-type` is the source type for Feature Toggles configuration - default: no configuration source is defined
- `features.refresh-period` configuration refresh period - default: `60s`
- `features.configuration-directory` feature toggle configuration sources configuration directory - default: `etc/feature-toggle`

### Feature Toggle configuration source configuration

For each configuration source we must add a configuration properties file in `features.configuration-directory`. Each configuration source can have their own set of properties.
For default configuration source we could add a `file.configuration` properties in directory containing configuration source configurations.

Example configuration for `file` configuration source `etc/feature-toggle/file.configuration` :

```
   features.config-source-type=file
   features.config-source=/etc/feature-config.properties
   features.config-type=properties
```

- `configuration-source-type` is the source type for Feature Toggles configuration - should be the same as file name
- `features.config-source` is a source (file) of the configuration
- `features.config-type` format in which configuration is stored (JSON or properties)

### Adding new configuration source

New configuration source must extend `com.facebook.presto.spi.features.ConfigurationSource`.
We must also define `com.facebook.presto.spi.features.ConfigurationSourceFactory` and register factory through plugin mechanism.

The `com.facebook.presto.features.plugin.FeatureToggleFileConfigurationSource`:

```
/**
 * The "file" Feature Toggle configuration source.
 * Configuration source loads Feature toggle configuration form file.
 * File can be in properties or json format.
 * Configuration Source takes two parameter type (properties or json) and location (physical location of the file)
 */
public class FeatureToggleFileConfigurationSource
        implements ConfigurationSource
{   
    public static final String NAME = "file"; \\ the name of the configuration source
    public static final String FEATURES_CONFIG_SOURCE = "features.config-source"; \\ configuration source configuration property
    public static final String FEATURES_CONFIG_SOURCE_TYPE = "features.config-type";  \\ configuration source configuration property

    private final String location;
    private final String type;

    public FeatureToggleFileConfigurationSource(String location, String type)
    {
        this.location = location;
        this.type = type;
    }

    @Override
    public FeatureToggleConfiguration getConfiguration()
    {
        return new DefaultFeatureToggleConfiguration(parseConfiguration(location, type));
    }

    // The configuration source factory  
    public static class Factory
            implements ConfigurationSourceFactory
    {
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public ConfigurationSource create(Map<String, String> config)
        {
            String location = config.get(FEATURES_CONFIG_SOURCE);
            checkState(location != null, "Configuration source path configuration must contain the '%s' property", FEATURES_CONFIG_SOURCE);
            String type = config.get(FEATURES_CONFIG_SOURCE_TYPE);
            checkState(type != null, "Configuration type configuration must contain the '%s' property", FEATURES_CONFIG_SOURCE_TYPE);
            return new FeatureToggleFileConfigurationSource(location, type);
        }
    }

```

## Defining Feature Toggles

Feature toggle definition is done in the Google guice module using `FeatureToggleBinder`

## Simple feature toggle definition

```
    featureToggleBinder(binder)
        .featureId("featureXX")
        .bind()
```

This example creates bindings for `@FeatureToggle("featureXX") Supplier<Boolean> isFeatureXXEnabled.`

```   
    @Inject
    public Runner(@FeatureToggle("featureXX") Supplier<Boolean> isFeatureXXEnabled)
    {
        this.isFeatureXXEnabled = isFeatureXXEnabled;
    }
```

Supplier&lt;Boolean&gt; `isFeatureXXEnabled` can be used to test if the feature is enabled or disabled:

```
    boolean testFeatureXXEnabled()
    {
     return isFeatureXXEnabled.get();
    }
```

Switching the feature toggle on/off is done by changing the enabled value from true to false in the configuration source file:

```
    feature.featureXX.enabled=true
```

After the refresh period value of `isFeatureXXEnabled.get();`  is changed.

## Hot reloadable feature toggle definition

```
    featureToggleBinder(binder, Feature01.class)
        .featureId("feature01")
        .baseClass(Feature01.class)
        .defaultClass(Feature01Impl01.class)
        .allOf(Feature01Impl01.class, Feature01Impl02.class)
        .bind()
```

```
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
```

## Strategy-based Feature Toggling

Strategy-based Toggling allows us to Implement custom predicates (Strategy Pattern) to evaluate if a feature is enabled.

Some are provided out of the box: AllowAll, OS-based toggle, and AllowList toggle strategy.

The current implementation allows us to define various strategies and register them during application initialization.

To use feature toggle strategies we must register strategy.
This should be used only once (subsequent registration doesn't have an effect).

```
    featureToggleBinder(binder)
        .registerToggleStrategy("AllowList", AllowListToggleStrategy.class)
        .bind();
```

Feature toggle definition with toggle strategy registration.

```
    public class RegisterStrategyModule
        implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            featureToggleBinder(binder)
                .featureId("query-cancel")
                .enabled(true)
                .toggleStrategy("AllowList")
                .registerToggleStrategy("AllowList", AllowListToggleStrategy.class)
                .toggleStrategyConfig(ImmutableMap.of("allow-list-source", ".IDEA.", "allow-list-user", ".*prestodb"))
                .bind();
        }
    }
```

Feature toggle definition with already registered toggle strategy.

```
    public class RegisterStrategyModule
        implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            featureToggleBinder(binder)
                .featureId("query-cancel")
                .enabled(true)
                .toggleStrategy("AllowList")
                .toggleStrategyConfig(ImmutableMap.of("allow-list-source", ".IDEA.", "allow-list-user", ".*prestodb"))
                .bind();
        }
    }
```

Toggle strategy configuration params can be updated on runtime, by changing strategy configuration param values.

In this case, we can change `feature.query-cancel.strategy.allow-list-source` and `feature.query-cancel.strategy.allow-list-user` param values.

# Examples

## Query Cancel Feature

```
    feature.query-cancel.enable=true
    feature.query-cancel.strategy=AllowList
    feature.query-cancel.strategy.allow-list-source=.*IDEA.*
    feature.query-cancel.strategy.allow-list-user=.*prestodb
```

Feature Toggle Strategies are evaluated each time we check if the feature toggle is enabled.
The result of the Feature Toggle Strategy evaluation overrides the `enabled` status of the Feature Toggle.

Simple Feature toggle check Example:

```
    class ClassWithQueryCancel
    {
        private final Supplier<Boolean> isQueryCancelEnabled;
        private final Function<Object, Boolean> isQueryCancelEnabledForQueryId;

        @Inject
        public SupplierInjectionRunner(
            @FeatureToggle("query-cancel") Supplier<Boolean> isQueryCancelEnabled,
            @FeatureToggle("FunctionInjectionFeature") Function<Object, Boolean> isQueryCancelEnabledForQueryId)
        {
            this.isQueryCancelEnabledForQueryId = isQueryCancelEnabledForQueryId;
            this.isQueryCancelEnabled = isQueryCancelEnabled;
        }

        /**
        * simple check: checks enabled param of the configuration
        */
        public boolean testSimpleFeatureEnabled()
        {
            return isQueryCancelEnabled.get();
        }

        /**
        * feature toggle check using a strategy that accepts input param
        */
         public boolean testFunctionInjectionFeatureEnabled(String queryId)
        {
            return isQueryCancelEnabledForQueryId.apply(queryId);
        }
    }
```

## Feature Toggle config file example

Any change of the configuration in the configuration file (source) overrides the feature toggle configuration.
Allowed configuration params are:

- enable
- strategy.<param>

```
# feature.query-cancel
feature.query-cancel.enable=true
feature.query-cancel.strategy=AllowList
feature.query-cancel.strategy.allow-list-source=.*IDEA.*
feature.query-cancel.strategy.allow-list-user=.*prestodb
```

Configuration properties always start with a `feature` followed by a dot and the feature id.

Feature Toggle strategy properties start with `feature.featureId.strategy`. Property `feature.featureId.strategy` defines feature toggle strategy class (declared by registered
name).
After that, we can declare key-value pairs for parameters allowed for a given strategy.

In this example for feature `query-cancel`, changing the value of feature.query-cancel.enabled to `false` will 'disable' this feature.
Changes will be effective within the refresh period. 
