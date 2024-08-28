# Content
* [About](#about)
* [errors.log](#errorslog)
* [placeholder-errors.log](#placeholder-errorslog)
* [anti-override.log](#anti-overridelog)

# About
TAB logs various events in up to 3 files to keep the console clean. All of these notifications are negative, so they should be looked at.

Files are only created when something is about to get logged. This means if you have none of these files in your folder, everything is good. Also, it is safe to delete the logs any time while server is running without needing to reload the plugin or anything.

All files have their sizes limited to 1MB to avoid giant files consisting of the same error repeating over and over. Don't forget these files are **logs**, therefore, after solving the problems you must delete them. These files will not delete themselves.  
After deleting a file, there is no need to perform any other action. Every time an error is about to be logged, file is created if it doesn't exist.

Java errors usually contain stack traces. If you got an error spamming with exception message but no stack trace, the most common cause is java not including it because too many errors were thrown already. To bypass this check, add `-XX:-OmitStackTraceInFastThrow` to your startup parameters.

# errors.log
No one likes having their console spammed with errors. Fixing errors is the most reliable solution, however that takes time and you usually don't get an update fixing an error the second you report it. Because of that, all plugin errors are moved to this file instead.

# placeholder-errors.log
This file is used to track errors thrown by PlaceholderAPI expansions when trying to retrieve value of a placeholder. These should be reported to the expansion author. First line(s) of error should give you a good idea which expansion it is, as well as the actual placeholder which threw the error (displayed on top of the error).

# anti-override.log
This file logs all attempts of other plugins to override TAB's visuals. This is checking for features that only 1 plugin can be displaying info in at once (such as nametag prefix/suffix, tablist prefix/suffix). This means that (at least) one of your plugins is not configured correctly and you didn't disable functions you don't want. If you want TAB to use a feature, disable it in other plugins. If you want a different plugin to display it, disable it in TAB (disabled features do not log this).

It is unfortunately not possible to easily figure out which plugin is causing it, you can only guess from the message.

Although this is technically unnecessary since TAB automatically blocks other plugins from overriding it, it's good to have your plugins configured properly. This might come in handy when moving TAB from bukkit to bungeecord where anti-override does not work too well due to missing functions in bungeecord, so you won't suddenly experience half of the features to not work when moving to bungeecord.