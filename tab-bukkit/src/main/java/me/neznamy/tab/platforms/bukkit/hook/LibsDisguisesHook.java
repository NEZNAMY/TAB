package me.neznamy.tab.platforms.bukkit.hook;

import me.libraryaddict.disguise.DisguiseAPI;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;

/**
 * Class for hooking into LibsDisguises to get disguise status of players.
 */
public class LibsDisguisesHook {

    /** Flag tracking if LibsDisguises is installed or not */
    private static boolean installed = ReflectionUtils.classExists("me.libraryaddict.disguise.DisguiseAPI");

    /**
     * Returns {@code true} if LibsDisguises is installed and player is disguised,
     * {@code false} otherwise.
     *
     * @param   player
     *          Player to check
     * @return  {@code true} if disguised, {@code false} otherwise
     */
    public static boolean isDisguised(TabPlayer player) {
        try {
            return installed && DisguiseAPI.isDisguised(((BukkitTabPlayer)player).getPlayer());
        } catch (LinkageError e) {
            //java.lang.NoClassDefFoundError: Could not initialize class me.libraryaddict.disguise.DisguiseAPI
            installed = false;
            return false;
        }
    }
}
