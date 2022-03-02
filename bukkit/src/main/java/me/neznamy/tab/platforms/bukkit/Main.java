package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable(){
        getLogger().info(EnumChatFormat.color("&7Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")"));
        if (!isVersionSupported()){
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        BukkitPlatform platform = new BukkitPlatform(this);
        TAB.setInstance(new TAB(platform, ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]),
                Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")", getDataFolder(), getLogger()));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.UNKNOWN_SERVER_VERSION) {
            getLogger().info(EnumChatFormat.color("&cUnknown server version: " + Bukkit.getBukkitVersion() + "! Plugin may not work correctly."));
        }
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(platform), this);
        TAB.getInstance().load();
        Metrics metrics = new Metrics(this, 5304);
        metrics.addCustomChart(new SimplePie("unlimited_nametag_mode_enabled", () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS) ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie("placeholderapi", () -> Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie("permission_system", () -> TAB.getInstance().getGroupManager().getPlugin().getName()));
        metrics.addCustomChart(new SimplePie("server_version", () -> "1." + TAB.getInstance().getServerVersion().getMinorVersion() + ".x"));
        PluginCommand cmd = Bukkit.getPluginCommand("tab");
        if (cmd == null) return;
        TABCommand command = new TABCommand();
        cmd.setExecutor(command);
        cmd.setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        //null check due to L31 return making L34 not run
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
    
    /**
     * Initializes all used NMS classes, constructors, fields and methods.
     * Returns {@code true} if everything went successfully and version is marked as compatible,
     * {@code false} if anything went wrong or version is not marked as compatible.
     * @return    {@code true} if server version is compatible, {@code false} if not
     */
    private boolean isVersionSupported(){
        List<String> supportedVersions = Arrays.asList(
                "v1_5_R1", "v1_5_R2", "v1_5_R3", "v1_6_R1", "v1_6_R2", "v1_6_R3",
                "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2", "v1_8_R3",
                "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1", "v1_13_R1", "v1_13_R2",
                "v1_14_R1", "v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2");
        String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            long time = System.currentTimeMillis();
            NMSStorage.setInstance(new NMSStorage());
            if (supportedVersions.contains(serverPackage)) {
                getLogger().info(EnumChatFormat.color("&7Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms"));
                return true;
            } else {
                getLogger().info(EnumChatFormat.color("&cNo compatibility issue was found, but this plugin version does not claim to support your server package (" + serverPackage + "). This jar has only been tested on 1.5.x - 1.18.2. Disabling just to stay safe."));
            }
        } catch (Exception ex) {
            if (supportedVersions.contains(serverPackage)) {
                getLogger().log(Level.SEVERE, EnumChatFormat.color("&cYour server version is marked as compatible, but a compatibility issue was found. Please report the error below (include your server version & fork too)"), ex);
            } else {
                getLogger().info(EnumChatFormat.color("&cYour server version is completely unsupported. This plugin version only supports 1.5.x - 1.18.2. Disabling."));
            }
        }
        return false;
    }

    /**
     * Command handler for /tab command
     */
    public static class TABCommand implements CommandExecutor, TabCompleter {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            if (TAB.getInstance().isDisabled()) {
                for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                    sender.sendMessage(EnumChatFormat.color(message));
                }
            } else {
                TabPlayer p = null;
                if (sender instanceof Player) {
                    p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
                    if (p == null) return true; //player not loaded correctly
                }
                TAB.getInstance().getCommand().execute(p, args);
            }
            return false;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
            TabPlayer p = null;
            if (sender instanceof Player) {
                p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
                if (p == null) return new ArrayList<>(); //player not loaded correctly
            }
            return TAB.getInstance().getCommand().complete(p, args);
        }
    }
}