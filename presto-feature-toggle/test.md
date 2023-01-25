# Presto feature toggles

should allow teams to modify system behavior without changing code.

Feature Toggles are configured using google guice. The basic definition of toggles is created using FeatureToggleBinder. FeatureToggleBinder creates FeatureToggle and additional configuration can be done using feature configuration.

In the current stage Feature Toggles support:

- if/else based feature toggles

- Dependency Injection based Hot reloading implementation without restart requires code refactoring to add an interface when injecting the new implementation/class

- using various toggle strategies along with simple on/off toggles

Feature Toggles uses google guice to inject feature toggles in code.

Feature Toggle injections are marked with @FeatureToggle(“featureId”) annotation.


# Root Configuration

To allow feature toggle configuration four lines are needed in the config.properties file


```
features.config-source-type=file
features.config-source=/etc/feature-config.properties
features.config-type=properties
features.refresh-period=30s
```


`configuration-source-type` is the source type for Feature Toggles configuration `features.config-source` is a source (file) of the configuration `features.config-type` format in which configuration is stored (JSON or properties) `features.refresh-period` configuration refresh period.


# 


# Defining Feature Toggles

Feature toggle definition is done in the google guice module using `FeatureToggleBinder`


## Simple feature toggle definition


```
    featureToggleBinder(binder)
                        .featureId("featureXX")
                        .bind()
```


This example creates bindings for `@FeatureToggle("featureXX") Supplier&lt;Boolean> isFeatureXXEnabled.`


```
    @Inject
    public Runner(
        @FeatureToggle("featureXX") Supplier<Boolean> isFeatureXXEnabled)
    {
        this.isFeatureXXEnabled = isFeatureXXEnabled;
    }
```


Supplier&lt;Boolean> `isFeatureXXEnabled` can be used to test if the feature is enabled or disabled:


```
    boolean testFeatureXXEnabled()
    {
        return isFeatureXXEnabled.get();
    }
```


Switching feature toggle on/off is done by changing the enabled value from true to false in the configuration source file:


```

feature.featureXX.enabled=true
```


\
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



## 


## Strategy-based Toggling

Strategy-based Toggling allows us to Implement custom predicates (Strategy Pattern) to evaluate if a feature is enabled. Some are provided out of the box: AllowAll, OS-based toggle, and AllowList toggle strategy. The current implementation allows us to define various strategies and register them during application initialization.


```
public class RegisterStrategyModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
      featureToggleBinder(binder)
        .registerToggleStrategy(ALLOW_LIST, AllowListToggleStrategy.class);
    }
}
```



```
featureToggleBinder(binder)
                    .featureId("feature04")
                    .toggleStrategy("AllowAll")
                    .toggleStrategyConfig(ImmutableMap.of("key", "value", "key2", "value2"))
```



### definition


## validation


### DI


### implementation switching


### strategy params


# configuration


## configuration source


### file


### memory


### other


## allowed configuration values


## order of configuration settings


### enable / disable


#### configuration


### strategy params


#### configuration


### current implementation


#### configuration


## settings overriding


### default values

enabled = true

hot-reloadable = false

strategy = AllowAll


### implicit configuration values


#### enabled

if implementation switching

if DI


#### hot-reloadable

if default implementation


#### default implemetation

explicit

current

first in list


## strategy

params


# features


## if / else toggle


## strategy toggle


## hot reload


## DI toggle

provider

direct


## implementation switching


### provider


### fallback


### default

explicit default

first from list


### current

default


# toggle strategies


## and / or


## strategies


### allow all

default


### os toggle


### allow list


# tests


## configuration load


## configuration change


## binding


## strategy


## implementation switching


# Web Endpoint
