## Introduction  
Here it is. After countless requests to go opensource, I did it to make those people quiet. It worked.  
It's already been a year and I still can't see any positives of it. This way I just made it easier for everyone to make a plugin as good, so people can stop using it and switch to something better. Or just get premium version for free.  
  
  
## Compiling
Plugin can be compiled using maven. Once you've purchased the premium version, you can enable premium features by setting [this line to return true](https://github.com/NEZNAMY/TAB/blob/master/shared/src/main/java/me/neznamy/tab/shared/TAB.java#L82) and apply your code improvements. Enabling it without purchasing the plugin may result in the features not working correctly.  
**No support is provided for self-compiled jars, only for official compilations.** If you compiled it yourself, it is highly likely you modified something, which might be the reason why is the plugin not working. If you didn't modify anything, you can just get the plugin from official download locations.
  
  
## Links
Free version  
    MC-Market: https://www.mc-market.org/resources/20631/  
    Github releases: https://github.com/NEZNAMY/TAB/releases  
Premium version  
    Polymart: https://polymart.org/resource/484  
    MC-Market: https://www.mc-market.org/resources/14009/  
Bukkit bridge https://www.spigotmc.org/resources/83966/  
Wiki https://github.com/NEZNAMY/TAB/wiki  
Discord: https://discord.gg/EaSvdk6  


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
    <artifactId>TAB</artifactId>
    <version>2.9.2</version>
  </dependency>
</dependencies>
 ```
