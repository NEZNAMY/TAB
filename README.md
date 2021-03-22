## Introduction  
Here it is. After countless requests to go opensource, I did it to make those people quiet. It worked.  
It's already been a year and I still can't see any positives of it. This way I just made it easier for everyone to make a plugin as good, so people can stop using it and switch to something better. 
  
  
## Compiling
Plugin can be compiled using maven.

  
## Premium code
The source contains content of premium version as well. However, this must be activated by setting [this line to return true](https://github.com/NEZNAMY/TAB/blob/master/src/main/java/me/neznamy/tab/shared/TAB.java#L79).  
  
  
## Links
Free version
    Spigot (releases): https://www.spigotmc.org/resources/57806/  
    Pre-releases https://github.com/NEZNAMY/TAB/releases  
Premium version  
    Polymart (releases): https://polymart.org/resource/484  
    MC-Market (releases): https://www.mc-market.org/resources/14009/  
    Pre-releases in discord for verified buyers  
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