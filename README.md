## Introduction  
Here it is. After countless requests to go opensource, I did it to make those people quiet. It worked.  
It's already been a year and I still can't see any positives of it. This way I just made it easier for everyone to make a plugin as good, so people can stop using it and switch to something better.  
If you have a great argument why was making this open-source worth not only for those looking for premium features for free, please take your time and explain it to: Lionel#5648 (more names coming soon).  
  
  
## Compiling
Plugin can be compiled using maven. Once you've purchased the premium version, you can enable premium features by setting [this line to return true](https://github.com/NEZNAMY/TAB/blob/master/shared/src/main/java/me/neznamy/tab/shared/TAB.java#L78) and apply your code improvements. Enabling it without purchasing the plugin may result in the features not working correctly.  
**No support is provided for self-compiled jars, only for official compilations.** If you compiled it yourself, it is highly likely you modified something, which might be the reason why is the plugin not working. If you didn't modify anything, you can just get the plugin from official download places.
  
  
## Links
Free version  
    Spigot (releases): https://www.spigotmc.org/resources/57806/  
    Pre-releases https://github.com/NEZNAMY/TAB/releases  
Premium version  
    Polymart (releases): https://polymart.org/resource/484  
    MC-Market (releases): https://www.mc-market.org/resources/14009/  
    Pre-releases on discord for verified buyers  
Bukkit bridge https://www.spigotmc.org/resources/83966/  
Wiki https://github.com/NEZNAMY/TAB/wiki  
Discord invite link: https://discord.gg/EaSvdk6  
  
  
## Downloads
Official release: https://www.spigotmc.org/resources/57806/  
Pre-releases: https://github.com/NEZNAMY/TAB/releases  
  
  
## Maven Repository
```
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <!-- TAB -->
  <dependency>
    <groupId>com.github.NEZNAMY</groupId>
    <artifactId>TAB-API</artifactId>
    <version>2.8.10</version>
  </dependency>
</dependencies>
 ```