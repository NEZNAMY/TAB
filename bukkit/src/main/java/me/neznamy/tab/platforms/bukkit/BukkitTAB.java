package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for Bukkit.
 */
public class BukkitTAB extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!isVersionSupported()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        TAB.create(ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer") ?
                new FoliaPlatform(this) : new BukkitPlatform(this));
    }

    @Override
    public void onDisable() {
        //null check due to compatibility check making instance not get set on unsupported versions
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
    
    /**
     * Initializes all used NMS classes, constructors, fields and methods.
     * Returns {@code true} if everything went successfully,
     * {@code false} if anything went wrong.
     *
     * @return  {@code true} if server version is compatible, {@code false} if not
     */
    private boolean isVersionSupported() {
        try {
            NMSStorage.setInstance(new NMSStorage());
            return true;
        } catch (Exception ex) {
            if (ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]) == ProtocolVersion.UNKNOWN_SERVER_VERSION) {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Your server version is not compatible. " +
                        "This plugin version was made for " + ProtocolVersion.values()[ProtocolVersion.values().length-1].getFriendlyName() +
                        " - " + ProtocolVersion.values()[3].getFriendlyName() + ". Disabling."));
            } else {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] A compatibility issue " +
                        "with your server was found. Unless you are running some really weird server software, this is a bug."));
            }
            return false;
        }
    }
}