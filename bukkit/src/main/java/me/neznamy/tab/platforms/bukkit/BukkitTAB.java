package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.*;
import me.neznamy.tab.shared.TAB;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Main class for Bukkit platform
 */
public class BukkitTAB extends JavaPlugin {

    @Override
    public void onEnable() {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("[TAB] Server version: " + version + " (" + serverPackage + ")"));
        if (!isVersionSupported()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        BukkitPlatform platform = new BukkitPlatform(this);
        TAB.setInstance(new TAB(platform, ProtocolVersion.fromFriendlyName(version), getDataFolder()));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.UNKNOWN_SERVER_VERSION) {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Unknown server version: " + Bukkit.getBukkitVersion() + "! Plugin may not work correctly."));
        }
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this);
        TAB.getInstance().load();
        Metrics metrics = new Metrics(this, 5304);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.UNLIMITED_NAME_TAG_MODE_ENABLED, () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS) ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.PLACEHOLDER_API, () -> platform.isPlaceholderAPI() ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.PERMISSION_SYSTEM, () -> TAB.getInstance().getGroupManager().getPermissionPlugin()));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION, () -> "1." + TAB.getInstance().getServerVersion().getMinorVersion() + ".x"));
        PluginCommand cmd = Bukkit.getPluginCommand(TabConstants.COMMAND_BACKEND);
        if (cmd == null) return;
        BukkitTabCommand command = new BukkitTabCommand();
        cmd.setExecutor(command);
        cmd.setTabCompleter(command);
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
            long time = System.currentTimeMillis();
            NMSStorage.setInstance(getNMSLoader());
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("[TAB] Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms"));
            return true;
        } catch (IllegalStateException ex) {
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

    private @NotNull NMSStorage getNMSLoader() {
        List<Callable<NMSStorage>> loaders = Arrays.asList(
                BukkitLegacyNMSStorage::new,
                BukkitModernNMSStorage::new,
                MojangModernNMSStorage::new,
                ThermosNMSStorage::new
        );
        for (Callable<NMSStorage> loader : loaders) {
            try {
                return loader.call();
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException("Unsupported server version");
    }
}