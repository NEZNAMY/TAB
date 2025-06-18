package me.neznamy.tab.platforms.bukkit.platform;

import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.chat.component.*;
import me.neznamy.tab.platforms.bukkit.*;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.ViaBossBar;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.hook.BukkitPremiumVanishHook;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.bukkit.BukkitImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.bukkit.PaperScoreboard;
import me.neznamy.tab.platforms.bukkit.provider.reflection.ReflectionImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaVersionProvider;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import me.neznamy.tab.shared.platform.impl.DummyBossBar;
import me.neznamy.tab.shared.util.PerformanceUtil;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.audience.Audience;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of Platform interface for Bukkit platform
 */
@Getter
public class BukkitPlatform implements BackendPlatform {

    /** Plugin instance for registering tasks and events */
    @NotNull
    private final JavaPlugin plugin;

    /** Server version */
    @Getter
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);

    /** Variables checking presence of other plugins to hook into */
    private final boolean placeholderAPI = ReflectionUtils.classExists("me.clip.placeholderapi.PlaceholderAPI");

    /** Spigot field for tracking TPS, the array is final and only being modified instead of re-instantiated */
    private double[] recentTps;

    /** Detection for presence of Paper's TPS getter */
    private final boolean paperTps = ReflectionUtils.methodExists(Bukkit.class, "getTPS");

    /** Detection for presence of Paper's MSPT getter */
    private final boolean paperMspt = ReflectionUtils.methodExists(Bukkit.class, "getAverageTickTime");

    /** Implementation for creating new instances using content available on the server */
    @NotNull
    private final ImplementationProvider serverImplementationProvider = findImplementationProvider();

    /** Implementation for sending new content to new players on old servers */
    @Nullable
    private final ViaVersionProvider viaVersionProvider =
            ReflectionUtils.classExists("com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3") ?
                    new ViaVersionProvider(serverVersion) : null;

    private final boolean modernOnlinePlayers;

    /**
     * Constructs new instance with given plugin.
     *
     * @param   plugin
     *          Plugin
     */
    @SneakyThrows
    public BukkitPlatform(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        modernOnlinePlayers = Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class;
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            recentTps = ((double[]) server.getClass().getField("recentTps").get(server));
        } catch (ReflectiveOperationException ignored) {
            //not spigot
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            new BukkitPremiumVanishHook().register();
        }
    }

    @NotNull
    @SneakyThrows
    private ImplementationProvider findImplementationProvider() {
        // Check for mojang-mapped paper (1.20.5+)
        String paperModule = getPaperModule();
        if (paperModule != null) {
            return (ImplementationProvider) Class.forName("me.neznamy.tab.platforms.paper_" + paperModule + ".PaperImplementationProvider").getConstructor().newInstance();
        }

        // Check for direct NMS on some supported versions
        String serverPackage = BukkitReflection.getServerVersion().getServerPackage();
        if (Arrays.asList("v1_8_R3", "v1_12_R1", "v1_16_R3", "v1_17_R1", "v1_18_R2", "v1_19_R1").contains(serverPackage) && serverVersion != ProtocolVersion.V1_19) {
            return (ImplementationProvider) Class.forName("me.neznamy.tab.platforms.bukkit." + serverPackage + ".NMSImplementationProvider").getConstructor().newInstance();
        }

        // Try reflection
        try {
            return new ReflectionImplementationProvider();
        } catch (Throwable e) {
            if (serverVersion.getMinorVersion() >= 8) {
                List<String> missingFeatures = new ArrayList<>();

                // Scoreboard
                missingFeatures.add("Compatibility with other scoreboard plugins being reduced");
                if (!PaperScoreboard.isAvailable()) {
                    missingFeatures.add("Features receiving new artificial character limits");
                    missingFeatures.add("1.20.3+ scoreboard visuals not working due to lack of API");
                }
                missingFeatures.add("Anti-override for nametags not working");

                // Tablist
                missingFeatures.add("Layout feature will not work");
                missingFeatures.add("Prevent-spectator-effect feature will not work");
                missingFeatures.add("Ping spoof feature will not work");
                missingFeatures.add("Tablist formatting missing anti-override");
                missingFeatures.add("Tablist formatting not supporting relational placeholders");
                missingFeatures.add("Compatibility with nickname plugins changing player names will not work");
                missingFeatures.add("Anti-override for tablist not working");
                missingFeatures.add("Header/Footer may be limited or not work at all"); // Maybe add some checks for a more accurate message

                Bukkit.getConsoleSender().sendMessage("§c[TAB] Your server version is not fully supported. This will result in:");
                for (String message : missingFeatures) {
                    Bukkit.getConsoleSender().sendMessage("§c[TAB] - " + message);
                }
                Bukkit.getConsoleSender().sendMessage("§c[TAB] Please use " +
                        "a plugin version with full support for your server version for optimal experience. This plugin version " +
                        "has full support for 1.8.8, 1.12.x, 1.16.5, 1.17.x, 1.18.2 and 1.19.1 - 1.21.6.");
            }
            return new BukkitImplementationProvider();
        }
    }

    /**
     * Returns name of the paper module that can be used on this server.
     * If this server is not using paper or no module is available for any other reason,
     * {@code null} is returned.
     *
     * @return  Name of the available paper module or {@code null} if not available
     */
    @Nullable
    private String getPaperModule() {
        if (!ReflectionUtils.classExists("org.bukkit.craftbukkit.CraftServer")) return null;
        switch (serverVersion) {
            case V1_20_5:
            case V1_20_6:
            case V1_21:
            case V1_21_1:
                return "1_20_5";
            case V1_21_2:
            case V1_21_3:
                return "1_21_2";
            case V1_21_4:
            case V1_21_5:
            case V1_21_6:
                return "1_21_4";
            default:
                return null;
        }
    }

    @Override
    public void loadPlayers() {
        for (Player p : getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer(this, p));
        }
    }

    @Override
    public void registerPlaceholders() {
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        manager.registerInternalServerPlaceholder("%vault-prefix%", -1, () -> "");
        manager.registerInternalServerPlaceholder("%vault-suffix%", -1, () -> "");
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) {
                Chat chat = rspChat.getProvider();
                manager.registerInternalPlayerPlaceholder("%vault-prefix%", 1000, p -> chat.getPlayerPrefix((Player) p.getPlayer()));
                manager.registerInternalPlayerPlaceholder("%vault-suffix%", 1000, p -> chat.getPlayerSuffix((Player) p.getPlayer()));
            }
        }
        // Override for the PAPI placeholder to prevent console errors on unsupported server versions when ping field changes
        manager.registerPlayerPlaceholder("%player_ping%", p -> PerformanceUtil.toString(((TabPlayer) p).getPing()));
        BackendPlatform.super.registerPlaceholders();
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return serverImplementationProvider.getChannelFunction() != null ? new BukkitPipelineInjector() : null;
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        if (placeholderAPI) {
            BukkitTabExpansion expansion = new BukkitTabExpansion();
            expansion.register();
            return expansion;
        }
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return new PerWorldPlayerList(plugin, this, configuration);
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        if (!placeholderAPI) {
            registerDummyPlaceholder(identifier);
            return;
        }
        if (identifier.startsWith("%rel_")) {
            //relational placeholder
            TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, (viewer, target) ->
                    PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier));
        } else if (identifier.startsWith("%sync:")) {
            registerSyncPlaceholder(identifier);
        } else if (identifier.startsWith("%server_")) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier,
                    () -> PlaceholderAPI.setPlaceholders(null, identifier));
        } else {
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier,
                    p -> PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), identifier));
        }
    }

    /**
     * Registers a sync placeholder with given identifier and automatically decided refresh.
     *
     * @param   identifier
     *          Placeholder identifier
     */
    public void registerSyncPlaceholder(@NotNull String identifier) {
        String syncedPlaceholder = "%" + identifier.substring(6);
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                long time = System.nanoTime();
                ppl[0].updateValue(p, placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier);
                long totalTime =  System.nanoTime()-time;
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
                TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REQUEST, totalTime);
            });
            return null;
        });
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        Bukkit.getConsoleSender().sendMessage("[TAB] " + toBukkitFormat(message));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        Bukkit.getConsoleSender().sendMessage("§c[TAB] [WARN] " + toBukkitFormat(message));
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Bukkit] " + Bukkit.getName() + " - " + Bukkit.getBukkitVersion().split("-")[0];
    }

    @Override
    public void registerListener() {
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), plugin);
    }

    @Override
    public void registerCommand() {
        PluginCommand command = Bukkit.getPluginCommand(getCommand());
        if (command != null) {
            BukkitTabCommand cmd = new BukkitTabCommand();
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        } else {
            logWarn(TabComponent.legacyText("Failed to register command, is it defined in plugin.yml?"));
        }
    }

    @Override
    public void startMetrics() {
        Metrics metrics = new Metrics(plugin, TabConstants.BSTATS_PLUGIN_ID_BUKKIT);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.PERMISSION_SYSTEM,
                () -> TAB.getInstance().getGroupManager().getPermissionPlugin()));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION,
                () -> "1." + serverVersion.getMinorVersion() + ".x"));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    @NotNull
    public Object convertComponent(@NotNull TabComponent component) {
        if (serverImplementationProvider.getComponentConverter() != null) {
            return serverImplementationProvider.getComponentConverter().convert(component);
        } else {
            return component;
        }
    }

    @Override
    @NotNull
    @SneakyThrows
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        if (viaVersionProvider != null) {
            Scoreboard scoreboard = viaVersionProvider.newScoreboard((BukkitTabPlayer) player);
            if (scoreboard != null) return scoreboard;
        }
        return serverImplementationProvider.newScoreboard((BukkitTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        //noinspection ConstantValue
        if (AdventureBossBar.isAvailable() && Audience.class.isAssignableFrom(Player.class)) return new AdventureBossBar(player);

        // 1.9+ server, handle using API, potential 1.8 players are handled by ViaVersion
        if (BukkitBossBar.isAvailable()) return new BukkitBossBar((BukkitTabPlayer) player);

        // 1.9+ player on 1.8 server, handle using ViaVersion API
        if (player.getVersion().getMinorVersion() >= 9) return new ViaBossBar((BukkitTabPlayer) player);

        // 1.8- server and player, no implementation
        return new DummyBossBar();
    }

    @Override
    @NotNull
    @SneakyThrows
    public TabList createTabList(@NotNull TabPlayer player) {
        if (viaVersionProvider != null) {
            TabList tabList = viaVersionProvider.newTabList((BukkitTabPlayer) player);
            if (tabList != null) return tabList;
        }
        return serverImplementationProvider.newTabList((BukkitTabPlayer) player);
    }

    @Override
    public boolean supportsScoreboards() {
        return true;
    }

    @Override
    public boolean isSafeFromPacketEventsBug() {
        return serverVersion.getMinorVersion() >= 13;
    }

    @Override
    @NotNull
    public GroupManager detectPermissionPlugin() {
        if (LuckPermsHook.getInstance().isInstalled()) {
            return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
            if (provider != null && !provider.getProvider().getName().equals("SuperPerms")) {
                return new GroupManager(provider.getProvider().getName(), p -> provider.getProvider().getPrimaryGroup((Player) p.getPlayer()));
            }
        }
        return new GroupManager("None", p -> TabConstants.NO_GROUP);
    }

    @Override
    public double getTPS() {
        if (recentTps != null) {
            return recentTps[0];
        } else if (paperTps) {
            return Bukkit.getTPS()[0];
        } else {
            return -1;
        }
    }

    @Override
    public double getMSPT() {
        if (paperMspt) return Bukkit.getAverageTickTime();
        return -1;
    }

    /**
     * Runs task in the main thread for given entity.
     *
     * @param   entity
     *          Entity's main thread
     * @param   task
     *          Task to run
     */
    public void runSync(@NotNull Entity entity, @NotNull Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Converts component to string using bukkit RGB format if supported by the server.
     * If not, closest legacy color is used instead.
     *
     * @param   component
     *          Component to convert
     * @return  Converted string using bukkit color format
     */
    @NotNull
    public String toBukkitFormat(@NotNull TabComponent component) {
        StringBuilder sb = new StringBuilder();
        if (component.getModifier().getColor() != null) {
            if (serverVersion.supportsRGB()) {
                String hexCode = component.getModifier().getColor().getHexCode();
                sb.append('§').append("x").append('§').append(hexCode.charAt(0)).append('§').append(hexCode.charAt(1))
                        .append('§').append(hexCode.charAt(2)).append('§').append(hexCode.charAt(3))
                        .append('§').append(hexCode.charAt(4)).append('§').append(hexCode.charAt(5));
            } else {
                sb.append('§').append(component.getModifier().getColor().getLegacyColor().getCharacter());
            }
        }
        sb.append(component.getModifier().getMagicCodes());
        if (component instanceof TextComponent) {
            sb.append(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            sb.append(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            sb.append(((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }
        for (TabComponent extra : component.getExtra()) {
            sb.append(toBukkitFormat(extra));
        }
        return sb.toString();
    }

    /**
     * Returns online players from Bukkit API.
     * This method may use reflections, because the return type changed in 1.7.10,
     * and we want to avoid errors.
     *
     * @return  Online players from Bukkit API.
     */
    @SneakyThrows
    @NotNull
    public Collection<? extends Player> getOnlinePlayers() {
        if (modernOnlinePlayers) {
            return Bukkit.getOnlinePlayers();
        }
        return Arrays.asList((Player[]) Bukkit.class.getMethod("getOnlinePlayers").invoke(null));
    }
}