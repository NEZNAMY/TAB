## Introduction  
Here it is. After countless requests to go opensource, I did it to make those people quiet. It worked.  
It's already been a year and I still can't see any positives of it. This way I just made it easier for everyone to make a plugin as good, so people can stop using it and switch to something better. 
  
  
## Compiling
Plugin can be compiled using maven. However, you will need to manually add velocity jar into /jars/ folder as defined in pom.xml due to plugin sending packets which are not available on maven. You can find download link in [.dependencies](https://github.com/NEZNAMY/TAB/blob/master/.dependencies) file.
  
  
## Contributing
You are allowed to make pull requests if you believe you have useful code changes that will improve the plugin for it's users (not just you). Make sure you test the changes first and don't break anything, such as by injecting bukkit api calls into universal code that will *crash* on bungeecord.  
  
  
## Premium code
The source contains content of premium version as well. However, this must be activated by setting [this line to return true](https://github.com/NEZNAMY/TAB/blob/master/src/main/java/me/neznamy/tab/premium/Premium.java#L22). Keep in mind you will get no support for premium features if you did not buy it. If you don't want to support me then I don't see a reason to provide support to you either.  
  
  
## Links
Discord: https://discord.gg/EaSvdk6  
SpigotMC (free): https://www.spigotmc.org/resources/57806/  
SpigotMC (paid): https://www.spigotmc.org/resources/83967/  
Wiki: https://github.com/NEZNAMY/TAB/wiki  
