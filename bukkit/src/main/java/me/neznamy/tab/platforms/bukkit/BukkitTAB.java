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
                    "This jar only supports 1.7 - 1.21.10. If you just updated to a new Minecraft version, check for TAB updates.");
            Bukkit.getConsoleSender().sendMessage("§c[TAB] ================================================================================");
        }
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() == null) return;
        TAB.getInstance().unload();
    }
}