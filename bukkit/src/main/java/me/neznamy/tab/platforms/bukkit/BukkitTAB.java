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

import java.util.ArrayList;
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
     * Returns {@code true} if everything went successfully and version is marked as compatible,
     * {@code false} if anything went wrong or version is not marked as compatible.
     *
     * @return  {@code true} if server version is compatible, {@code false} if not
     */
    private boolean isVersionSupported() {
        List<String> supportedVersions = Arrays.asList(
                "v1_5_R1", "v1_5_R2", "v1_5_R3", "v1_6_R1", "v1_6_R2", "v1_6_R3",
                "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2", "v1_8_R3",
                "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1", "v1_13_R1", "v1_13_R2",
                "v1_14_R1", "v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3", "v1_17_R1", "v1_18_R1",
                "v1_18_R2", "v1_19_R1", "v1_19_R2", "v1_19_R3", "v1_20_R1");
        String supportedVersionRange = "1.5 - 1.20";
        String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            long time = System.currentTimeMillis();
            NMSStorage.setInstance(getNMSLoader());
            if (supportedVersions.contains(serverPackage)) {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("[TAB] Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms"));
                return true;
            } else {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] No compatibility issue was found, but this plugin version does not claim to support your server package (" + serverPackage + "). This jar has only been tested on " + supportedVersionRange + ". Disabling just to stay safe."));
            }
        } catch (IllegalStateException ex) {
            if (supportedVersions.contains(serverPackage)) {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Your server version is marked as compatible, but a compatibility issue was found. Please report this issue (include your server version & fork too)"));
            } else {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Your server version is completely unsupported. This plugin version only supports " + supportedVersionRange + ". Disabling."));
            }
        }
        return false;
    }

    private @NotNull NMSStorage getNMSLoader() {
        List<Callable<NMSStorage>> loaders = new ArrayList<>();
        loaders.add(BukkitLegacyNMSStorage::new);
        loaders.add(BukkitModernNMSStorage::new);
        loaders.add(MojangModernNMSStorage::new);
        loaders.add(ThermosNMSStorage::new);
        for (Callable<NMSStorage> loader : loaders) {
            try {
                return loader.call();
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException("Unsupported server version");
    }
}