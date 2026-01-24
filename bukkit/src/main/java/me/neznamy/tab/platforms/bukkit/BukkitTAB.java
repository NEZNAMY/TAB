package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
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
        boolean folia = ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
        try {
            TAB.create(folia ? new FoliaPlatform(this) : new BukkitPlatform(this));
        } catch (UnsupportedOperationException e) {
            Bukkit.getConsoleSender().sendMessage("§c[TAB] ================================================================================");
            Bukkit.getConsoleSender().sendMessage("§c[TAB] Your server version (" + Bukkit.getBukkitVersion().split("-")[0] + ") is not supported. " +
                    "This jar only supports 1.7.10, 1.8.8, 1.9.4 - 1.15.x, 1.16.5, 1.17.1, 1.18.2 - 1.21.11. " +
                    "If you just updated to a new Minecraft version, check for TAB updates. " +
                    "If you are using an unsupported 1.x version, use an older version of TAB (latest TAB 5.x supports all MC 1.x versions).");
            Bukkit.getConsoleSender().sendMessage("§c[TAB] ================================================================================");
        }
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() == null) return;
        TAB.getInstance().unload();
    }
}