## Introduction  
Here it is. After countless requests to go opensource, I did it to make those people quiet. It worked.  
It's already been a year and I still can't see any positives of it. This way I just made it easier for everyone to make a plugin as good, so people can stop using it and switch to something better. 
  
  
## Compiling
Plugin can be compiled using maven.
  
  
## Contributing
You are allowed to make pull requests if you believe you have useful code changes that will improve the plugin for it's users (not just you). Make sure you test the changes first and don't break anything, such as by injecting bukkit api calls into universal code that will *crash* on bungeecord.  
  
  
## Premium code
The source contains content of premium version as well. However, this must be activated by setting [this line to return true](https://github.com/NEZNAMY/TAB/blob/master/src/main/java/me/neznamy/tab/shared/TAB.java#L79).  
  
  
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
## Links
Discord: https://discord.gg/EaSvdk6  
SpigotMC (free): https://www.spigotmc.org/resources/57806/  
MC-Market (paid): https://www.mc-market.org/resources/14009/
Wiki: https://github.com/NEZNAMY/TAB/wiki  
