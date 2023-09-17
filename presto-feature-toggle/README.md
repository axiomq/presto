# Feature Toggles

Feature Toggles should allow teams to modify system behavior without changing code. Feature Toggles are configured using google guice. Basic definition of toggles are crated using
FeatureToggleBinder. FeatureToggleBinder creates FeatureToggle and additional configuration can be done using feature configuration.
In current stage Feature Toggles supports:

- if / else based feature toggles
- using various toggle strategies along with simple on / off toggles
- Dependency Injection based Hot reloading implementation without restart - require code refactoring to add an interface when injecting the new implementation/class

## Contents

1. [Configuration](#configuration)
   1. [Configuration source type](#configuration-source-type-featuresconfig-source-type)
   2. [Configuration directory](#configuration-directory-featuresconfiguration-directory)
   3. [Configuration refresh period](#configuration-refresh-period-featuresrefresh-period)
2. [Configuration types](#feature-toggle-configuration-types)
   1. [Static Feature Toggle Configuration](#static-feature-toggle-configuration)
   2. [Dynamic Feature Toggle Configuration](#dynamic-feature-toggle-configuration)
3. [Feature Toggle Configuration Sources](#feature-toggle-configuration-sources)
4. [Feature Toggle Configuration Manager](#feature-toggle-configuration-manager)
5. [Configuration sources configuration files and loading](#configuration-sources-configuration-files-and-loading)
   1. [Default configuration source](#default-configuration-source)
   2. [File configuration source](#file-configuration-source)
6. [Feature Toggle configuration](#feature-toggle-configuration)
   1. [featureId - feature identifier](#featureid---feature-identifier)
   2. [enabled](#enabled)
   3. [featureClass - base interface class](#featureclass---base-interface-class)
   4. [default class](#default-class)
   5. [implementation classes](#implementation-classes)
   6. [current class](#current-class)
7. [Defining Feature Toggles](#defining-feature-toggles)
    1. [Simple feature toggle definition](#simple-feature-toggle-definition)
    2. [Hot reloadable feature toggle definition](#hot-reloadable-feature-toggle-definition)
    3. [Strategy-based Feature Toggling](#strategy-based-feature-toggling)
8. [Examples](#examples)
    1. [Query Cancel Feature](#query-cancel-feature)
    2. [feature-config.properties file example](#feature-toggle-config-file-example)

## Configuration

Feature toggles are enabled by default. We can add additional configuration parameters to the `config.properties` file

```
    features.config-source-type=file
    features.configuration-directory=etc/feature-toggle/
    features.refresh-period=30s
```

- `configuration-source-type` is the source type for Feature Toggles configuration - default `default`
- `features.configuration-directory` directory which contains individual configurations for declared source types - default `etc/feature-toggle/`
- `features.refresh-period` configuration refresh period - default `60s`

### Configuration source type `features.config-source-type`

We can add various configuration sources for Feature toggles. If no other configuration source is defined, Feature Toggle uses default (dummy) configuration source.
Configuration source provides dynamic Feature Toggle configuration.

### Configuration directory `features.configuration-directory`

Configuration directory is a directory in which we can add configuration source configuration. File format is `type-name.properties`.
Configuration property file must contain key `features.config-source-type` with value matching file name.
Example: `config.properties` file contains line `features.config-source-type=file` e.g. configuration source is a file.
in directory `etc/feature-toggle/` we must define a file `file.propereties`, and file must contain entry `features.config-source-type=file`.
Other config parameters depends on configuration source.

### Configuration refresh period `features.refresh-period`

Configuration refresh period is a period after which dynamic Feature Toggle configuration is refreshed.
Configuration refresh period is a string parsable by `io.airlift.units.Duration`.

## Feature Toggle Configuration types

Feature Toggle can have a static and dynamic configuration. Static configuration is defined in FeatureToggle definition phase.

```
    featureToggleBinder(binder, Feature01.class)
        .featureId("feature01")
        .baseClass(Feature01.class)
        .defaultClass(Feature01Impl01.class)
        .allOf(Feature01Impl01.class, Feature01Impl02.class)
        .toggleStrategy("AllowList")
        .registerToggleStrategy("AllowList", AllowListToggleStrategy.class)
        .toggleStrategyConfig(ImmutableMap.of("allow-list-source", ".IDEA.", "allow-list-user", ".*prestodb"))
        .bind()
```

Dynamic configuration is configuration loaded from configuration source. Dynamic configuration is reloaded after refresh period. Dynamic configuration

### Static Feature Toggle Configuration

Example of defining Feature Toggle:

```
    featureToggleBinder(binder, Feature01.class)
        .featureId("feature01")
        .enabled(true)
        .baseClass(FeatureInterface.class)
        .defaultClass(FeatureImplementation01.class)
        .allOf(FeatureImplementation01.class, FeatureImplementation02.class)
        .registerToggleStrategy("AllowList", AllowListToggleStrategy.class)
        .toggleStrategy("AllowList")
        .toggleStrategyConfig(ImmutableMap.of("allow-list-source", ".IDEA.", "allow-list-user", ".*prestodb"))
        .bind()
```

In process of defining Feature Toggle we can also define configuration parameters. Those parameters are part fo static Feature Toggle configuration.
Based of type of Feature Toggle we want to define, mandatory parameters can vary. We will talk about different types of Feature Toggles later in this document.

      .featureId("feature01") 

The 'featureId' is mandatory parameter and represent unique identifier for that Feature Toggle.

      .enabled(true)

We can enable or disable feature by setting parameter `enabled` to true or false. If configuration is omitted, Feature Toggle is enabled by default.
This configuration can be overriden by dynamic configuration.

      .baseClass(FeatureInterface.class)

Base class is a java interface we want to bind in Feature Toggle to implementation.
If we define base class we have also to define a default class:

      .defaultClass(FeatureImplementation01.class)

so we can create binding.

      // simple implementation binding
      binder.bind(baseClass).to(defaultClass);

To define hot reloading Feature Toggle we must define list of classes extending base class interface for which we can change implementation on runtime.

      .allOf(FeatureImplementation01.class, FeatureImplementation02.class)

If list of classes are provided, then Feature Toggles can bind base class interface to provider:

      // bind providers
      if (classes != null && !classes.isEmpty()) {
         binder.bind(baseClass).annotatedWith(FeatureToggles.named(featureId)).toProvider(() -> baseClass.cast(feature.getCurrentInstance(featureId)));
         configuration.setHotReloadable(true);
      }

With `featureToggleBinder` we can also register Feature Toggle Strategies.

      .toggleStrategy("AllowList")

`.toggleStrategy("AllowList")` is defining toggle strategy for this Feature Toggle

      .toggleStrategyConfig(ImmutableMap.of("allow-list-source", ".IDEA.", "allow-list-user", ".*prestodb"))

this line defines Feature Toggle configuration parameters.

### Dynamic Feature Toggle Configuration

In previous chapter we demonstrated how you can create Feature Toggles and define static configuration. Feature Toggles can also define dynamic configuration.
Dynamic Feature Toggle configuration overrides static configuration. Dynamic configuration is loaded using Feature Toggle configuration sources

## Feature Toggle Configuration Sources

Configuration source `ConfigurationSource` is responsible for connecting configuration source and parsing data to `FeatureToggleConfiguration`. Every configuration source can have
their own set of
parameters.

      public interface ConfigurationSource
      {
         FeatureToggleConfiguration getConfiguration();
      }

For creating configuration source we must define an instance of `ConfigurationSourceFactory` which accepts configuration parameters in form of `Map<String, String>`.
Configuration source must also override method `String getName()` which identifies configuration source.

      public interface ConfigurationSourceFactory
      {
         String getName();

         ConfigurationSource create(Map<String, String> config);
      }

## Feature Toggle Configuration Manager

`FeatureToggleConfigurationManager` is responsible for instantiation and configuration of configuration sources. To add a `ConfigurationSourceFactory` we must
call `FeatureToggleConfigurationManager.addConfigurationSourceFactory`. Plugin mechanism is used to add new configuration sources.
If no configuration source is defined a `DefaultConfigurationSource` is used.

## Configuration sources configuration files and loading

Configuration source configuration files are located in directory defined by FeatureToggleConfig.configDirectory property (`features.configuration-directory`).
When `FeatureToggleConfigurationManager.loadConfigurationSources()` method is invoked, `FeatureToggleConfigurationManager` loads all configuration files from config directory.
For each file we must have registered a configuration source factory with same name.
Example: `config.properties` file contains line `features.config-source-type=file` e.g. configuration source is a file.
in directory `etc/feature-toggle/` we must define a file `file.propereties`, and file must contain entry `features.config-source-type=file`. We must also have
registered `ConfigurationSourceFactory` with same name.

### Default configuration source

`DefaultConfigurationSource` is a default or 'dummy' configuration source. `DefaultConfigurationSource` will return a `NullFeatureConfiguration`. `NullFeatureConfiguration` doesn't
override any static configuration.

      public class DefaultConfigurationSource
         implements ConfigurationSource
      {
         public static final String NAME = "default";

         @Override
         public FeatureToggleConfiguration getConfiguration()
         {
            return new NullFeatureConfiguration();
         }
      ... 

### File configuration source

The `FeatureToggleFileConfigurationSource` is a configuration source that reads dynamic configuration from file. File can be in properties or JSON format.
Example file.properties file

      features.config-source-type=file
      features.config-source=/etc/feature-config.properties
      features.config-type=properties

File configuration source requires two parameters features.config-source and features.config-type.
feature-config.properties file may look like this:

      # feature query-logger
      feature.query-logger.enabled=false
      feature.query-logger.strategy=OsToggle
      feature.query-logger.strategy.os_name=.*Linux.*
      
      #feature.query-rate-limiter
      feature.query-rate-limiter.currentInstance=com.facebook.presto.server.protocol.AnotherQueryBlockingRateLimiter
      
      # feature.query-cancel
      feature.query-cancel.enabled=true
      feature.query-cancel.strategy=AllowList
      feature.query-cancel.strategy.allow-list-source=.*IDEA.*
      feature.query-cancel.strategy.allow-list-user=.*prestodb
      #feature.query-cancel.strategy.allow-list-source=.*cli.*
      #feature.query-cancel.strategy.allow-list-user=.*bane

Configuration property starts with a 'feature.' and followed by featureId.

In case of JSON configuration formatIf file.properties file looks like this:

      features.config-source-type=file
      features.config-source=/etc/feature-config.json
      features.config-type=json

feature-config.json may look like this:

      [
         {
            "featureId": "rate limiter",
            "enabled": true,
            "featureClass": "com.facebook.presto.server.protocol.QueryRateLimiter",
            "currentInstance": "com.facebook.presto.server.protocol.QueryBlockingRateLimiter",
            "featureInstances": [
               "com.facebook.presto.server.protocol.QueryBlockingRateLimiter",
               "com.facebook.presto.server.protocol.AnotherQueryBlockingRateLimiter"
            ]
         }
      ]

JSON structure reflects structure of the `FeatureConfiguration.class`

## Feature Toggle configuration

FeatureConfiguration class structure:

      public class FeatureConfiguration
      {
         private String featureId;
         private boolean enabled;
         private String featureClass;
         private boolean hotReloadable;
         private List<String> featureInstances;
         private String currentInstance;
         private String defaultInstance;
         private FeatureToggleStrategyConfig featureToggleStrategyConfig;
      ... 

getters, setters and constructor are omitted for readability. First field is a featureId.

### featureId - feature identifier

featureId is unique identifier used in FeatureToggle engine. Configuration for each feature is bound using featureId.

### enabled

Feature can be enabled or disabled using static configuration. If parameter is omitted - feature is enabled by default.
Feature can be also enabled or disabled using this property in dynamic configuration.

### featureClass - base interface class

`featureClass` defines base interface of the feature. `featureClass` is a static configuration property and cannot be overriden by dynamic configuration.
`featureClass` represents interface which will be bound using google guice.
Base class can be defined by calling static method `featureToggleBinder`

      featureToggleBinder(binder, Interface.class)

### default class

`defaultInstance` is a static configuration parameter and defines default instance of the base interface (`featureClass`).

      featureToggleBinder(binder, HotReloadFeature.class)
            .featureId("HotReloadFeature")                          // sets feature id
            .defaultClass(HotReloadFeatureImpl01.class)             // defines default implementation for the feature
            .bind());

For this example feature toggle binder will create simple binding

      // simple implementation binding
      binder.bind(baseClass).to(defaultClass);

### implementation classes

`featureInstances` is a static configuration parameter adn represents list of class implementations of `featureClass`. If list is defined then this Feature Toggle is
hot-reloadable, and we can change instance on runtime.

      featureToggleBinder(binder, HotReloadFeature.class)
         .featureId("HotReloadFeature")                          // sets feature id
         .baseClass(HotReloadFeature.class)                      // defines base interface of the feature
         .defaultClass(HotReloadFeatureImpl01.class)             // defines default implementation for the feature
         // defines list of implementations of the base interface that can be hot swapped on runtime
         .allOf(HotReloadFeatureImpl01.class, HotReloadFeatureImpl02.class)
         .bind());

List of implementation is defined using `allOf(Class<? extends T>... classes)` method od `featureToggleBinder`

### current class

If Feature is hot-reloadable we can use `currentInstance` configuration parameter to define current active Implementation of `featureClass` base interface.
`currentInstance` is a dynamic configuration parameter.

### Toggle Strategy

`toggleStrategy` (static and dynamic configuration parameter) is a configuration param which defines Feature Toggle strategy for current Feature Toggle.
`toggleStrategy` is a name of the registered Feature Toggle strategy.  

      featureToggleBinder(binder)
         .featureId("query-cancel")
         .enabled(true)
         .registerToggleStrategy("AllowList", AllowListToggleStrategy.class)
         .toggleStrategy("AllowList")
         .toggleStrategyConfig(ImmutableMap.of("allow-list-source", ".IDEA.", "allow-list-user", ".*prestodb"))
         .bind();

### Toggle Strategy Config



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

`Supplier<Boolean>` `isFeatureXXEnabled` can be used to test if the feature is enabled or disabled:

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
        public Runner(@FeatureToggle("feature01") Provider<Feature01> feature01)
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
