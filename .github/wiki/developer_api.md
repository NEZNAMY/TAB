# Page content
* [Adding the dependency](#adding-the-dependency)
* [Getting started](#getting-started)
* [Events](#Events)

# External Content
* [BossBar](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Bossbar#api)
* [Header/Footer](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Header-&-Footer#api)
* [Nametags](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags#api)
* [Scoreboard](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Scoreboard#api)
* [Sorting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#api)
* [Tablist name formatting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Tablist-name-formatting#api)
* [Placeholders](https://github.com/NEZNAMY/TAB/wiki/Placeholders#api)
* [Unlimited nametag mode](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Unlimited-nametag-mode#api)

# Adding the dependency

For Maven users, you can add the API dependency like this:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.NEZNAMY</groupId>
    <artifactId>TAB-API</artifactId>
    <version>4.1.6</version>
    <scope>provided</scope>
</dependency>
 ```

Or, for Gradle users, here's a few examples:

Groovy:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.NEZNAMY:TAB-API:4.1.6'
}
```

Kotlin:
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.NEZNAMY", "TAB-API", "4.1.6")
}
```

**WARNING: DO NOT SHADE THE API IN TO YOUR JAR! DO NOT RELOCATE THE API FROM ITS ORIGINAL PACKAGE!**

If you relocate, the API that you are calling will also get relocated in your code, meaning that you will be trying to call the relocated API. If you shade the API in, your code could load that bundled API, causing class path conflicts.
Ensure that you are using **`<scope>provided</scope>` for Maven**, or **`compileOnly` for Gradle**.

# Getting started
First of all, you need to get an instance of the API. You can do this with `TabAPI.getInstance()`.  
The `TabAPI` class that you want to import is located in the `me.neznamy.tab.api` package. If you do not see this imported, or it is imported but the package is not `me.neznamy.tab.api`, then you have the **wrong class**.

Now you have the instance, you can get a `TabPlayer` with the methods `getPlayer(UUID)` and `getPlayer(String)`, to get players by UUID and name respectively.  
If no player was found with the UUID or the name that you gave, these methods will return `null`. It is important that you catch this, to avoid running in to a `NullPointerException`. You should **NOT** assume that just because a player has logged in that TAB will already have a player for them, as players are processed asynchronously. This will be especially true for join events like `PlayerJoinEvent`.

If you want to be 100% sure that the player you want is loaded when you want to process them, use `PlayerLoadEvent`. This will give you the `TabPlayer` that has been loaded, so that you can process that `TabPlayer`. See below for more details how to use the event.

# Events
TAB is using a custom platform-independent event API. As such, the same event listener code will work on all supported platforms. Usage is also different, let's take a look at an example:
```
TabAPI.getInstance().getEventBus().register(PlayerLoadEvent.class, event -> {
    TabPlayer tabPlayer = event.getPlayer();
    //do something
});
```
Currently, the plugin provides 3 events:
* `TabLoadEvent` - Called when the plugin has finished its load, either on startup or plugin reload.
* `PlayerLoadEvent` - Called when the plugin has finished loading a player (either join or plugin reload).
* `PlaceholderRegisterEvent` - Called when the plugin is about to register a parser for used PlaceholderAPI placeholder, allowing to override it.