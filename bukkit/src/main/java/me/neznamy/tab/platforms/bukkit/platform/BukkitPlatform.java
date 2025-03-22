package me.neznamy.tab.platforms.bukkit.platform;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.chat.component.*;
import me.neznamy.tab.platforms.bukkit.*;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.ViaBossBar;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.header.*;
import me.neznamy.tab.platforms.bukkit.hook.BukkitPremiumVanishHook;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.converter.LegacyComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.converter.ModerateComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.converter.ModernComponentConverter;
import me.neznamy.tab.platforms.bukkit.scoreboard.BukkitScoreboard;
import me.neznamy.tab.platforms.bukkit.scoreboard.PaperScoreboard;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.platforms.bukkit.tablist.*;
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
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import me.neznamy.tab.shared.util.PerformanceUtil;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    /** Component converter from TAB to minecraft components */
    @Nullable
    private final ComponentConverter componentConverter = findComponentConverter();

    /** Provider for scoreboard implementation */
    @NotNull
    private final FunctionWithException<BukkitTabPlayer, Scoreboard> scoreboardProvider = findScoreboardProvider();

    /** Provider for tablist implementation */
    @NotNull
    private final FunctionWithException<BukkitTabPlayer, TabListBase> tablistProvider = findTablistProvider();

    /** Header/footer implementation */
    @Getter
    @NotNull
    private final HeaderFooter headerFooter = findHeaderFooter();

    /**
     * Constructs new instance with given plugin.
     *
     * @param   plugin
     *          Plugin
     */
    public BukkitPlatform(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        long time = System.currentTimeMillis();
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            recentTps = ((double[]) server.getClass().getField("recentTps").get(server));
        } catch (ReflectiveOperationException ignored) {
            //not spigot
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            new BukkitPremiumVanishHook().register();
        }
        PingRetriever.tryLoad();
        if (BukkitReflection.getMinorVersion() >= 8) {
            BukkitPipelineInjector.tryLoad();
        }
        BukkitUtils.sendCompatibilityMessage();
        Bukkit.getConsoleSender().sendMessage("[TAB] §7Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms");
    }

    @NotNull
    private FunctionWithException<BukkitTabPlayer, Scoreboard> findScoreboardProvider() {
        try {
            if (BukkitReflection.getMinorVersion() >= 7) Objects.requireNonNull(componentConverter);
            PacketScoreboard.load();
            return PacketScoreboard::new;
        } catch (Exception e) {
            if (PaperScoreboard.isAvailable()) {
                BukkitUtils.compatibilityError(e, "Scoreboards", "Paper API", "Compatibility with other plugins being reduced");
                return PaperScoreboard::new;
            } else if (BukkitScoreboard.isAvailable()) {
                List<String> missingFeatures = Lists.newArrayList(
                        "Compatibility with other plugins being reduced",
                        "Features receiving new artificial character limits"
                );
                if (BukkitReflection.is1_20_3Plus()) {
                    missingFeatures.add("1.20.3+ visuals not working due to lack of API"); // soontm?
                }
                BukkitUtils.compatibilityError(e, "Scoreboards", "Bukkit API", missingFeatures.toArray(new String[0]));
                return BukkitScoreboard::new;
            } else if (BukkitReflection.getMinorVersion() >= 5) {
                BukkitUtils.compatibilityError(e, "Scoreboards", null,
                        "Scoreboard feature will not work",
                        "Belowname feature will not work",
                        "Player objective feature will not work",
                        "Scoreboard teams feature will not work (nametags & sorting)");
            }
        }
        return DummyScoreboard::new;
    }

    @NotNull
    private FunctionWithException<BukkitTabPlayer, TabListBase> findTablistProvider() {
        try {
            if (ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket")) {
                // 1.19.3+
                Objects.requireNonNull(componentConverter);
                PacketTabList1193.loadNew();
                return PacketTabList1193::new;
            } else if (BukkitReflection.getMinorVersion() >= 8) {
                // 1.8 - 1.19.2
                Objects.requireNonNull(componentConverter);
                PacketTabList18.load();
                return PacketTabList18::new;
            } else {
                // 1.7.10 and lower
                PacketTabList17.load();
                return PacketTabList17::new;
            }
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "tablist entry management", "Bukkit API",
                    "Layout feature will not work",
                    "Prevent-spectator-effect feature will not work",
                    "Ping spoof feature will not work",
                    "Tablist formatting missing anti-override",
                    "Tablist formatting not supporting relational placeholders");
            return BukkitTabList::new;
        }
    }

    @NotNull
    private HeaderFooter findHeaderFooter() {
        if (BukkitReflection.getMinorVersion() >= 8) {
            try {
                Objects.requireNonNull(componentConverter);
                return new PacketHeaderFooter();
            } catch (Exception e) {
                if (PaperHeaderFooter.isAvailable()) return new PaperHeaderFooter();
                if (BukkitHeaderFooter.isAvailable()) {
                    BukkitUtils.compatibilityError(e, "sending Header/Footer", "Bukkit API",
                            "Header/Footer having drastically increased CPU usage",
                            "Header/Footer not supporting fonts (1.16+)");
                    return new BukkitHeaderFooter();
                } else {
                    BukkitUtils.compatibilityError(e, "sending Header/Footer", null,
                            "Header/Footer feature not working");
                }
            }
        }
        return new DummyHeaderFooter();
    }

    /**
     * Attempts to load component converter.
     *
     * @return  Instance or {@code null} if not available
     */
    @Nullable
    public static ComponentConverter findComponentConverter() {
        try {
            if (BukkitReflection.getMinorVersion() >= 19) {
                // 1.19+
                return new ModernComponentConverter();
            } else if (BukkitReflection.getMinorVersion() >= 16) {
                // 1.16 - 1.18.2
                return new ModerateComponentConverter();
            } else {
                // 1.7 - 1.15.2
                return new LegacyComponentConverter();
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c[TAB] Failed to initialize converter from TAB components to Minecraft components. " +
                    "This will negatively impact most features, see below.");
            if (BukkitUtils.PRINT_EXCEPTIONS) {
                e.printStackTrace();
            }
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
        return BukkitReflection.getMinorVersion() >= 8 && BukkitPipelineInjector.isAvailable() ? new BukkitPipelineInjector() : null;
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
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        if (identifier.startsWith("%rel_")) {
            // relational placeholder
            pl.registerRelationalPlaceholder(identifier, (viewer, target) ->
                    PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier));
        } else if (identifier.startsWith("%sync:")) {
            registerSyncPlaceholder(identifier);
        } else if (identifier.contains("{") && identifier.contains("}")) {
            // has nested bracket placeholders
            pl.registerPlayerPlaceholder(identifier, p -> PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), PlaceholderAPI.setBracketPlaceholders((Player) p.getPlayer(), identifier)));
        } else if (identifier.startsWith("%server_")) {
            // placeholder with the same output for all players, register as server for better performance
            pl.registerServerPlaceholder(identifier, () -> PlaceholderAPI.setPlaceholders(null, identifier));
        } else {
            pl.registerPlayerPlaceholder(identifier, p -> PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), identifier));
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
                ppl[0].updateValue(p, placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), PlaceholderAPI.setBracketPlaceholders((Player) p.getPlayer(), syncedPlaceholder)) : identifier);
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, System.nanoTime() - time);
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
            logWarn(SimpleTextComponent.text("Failed to register command, is it defined in plugin.yml?"));
        }
    }

    @Override
    public void startMetrics() {
        Metrics metrics = new Metrics(plugin, TabConstants.BSTATS_PLUGIN_ID_BUKKIT);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.PERMISSION_SYSTEM,
                () -> TAB.getInstance().getGroupManager().getPermissionPlugin()));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION,
                () -> "1." + BukkitReflection.getMinorVersion() + ".x"));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    @NotNull
    public Object convertComponent(@NotNull TabComponent component) {
        if (componentConverter != null) {
            return componentConverter.convert(component);
        } else {
            return component;
        }
    }

    @Override
    @NotNull
    @SneakyThrows
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return scoreboardProvider.apply((BukkitTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        //noinspection ConstantValue
        if (AdventureBossBar.isAvailable() && Audience.class.isAssignableFrom(Player.class)) return new AdventureBossBar(player);

        // 1.9+ server, handle using API, potential 1.8 players are handled by ViaVersion
        if (BukkitReflection.getMinorVersion() >= 9) return new BukkitBossBar((BukkitTabPlayer) player);

        // 1.9+ player on 1.8 server, handle using ViaVersion API
        if (player.getVersion().getMinorVersion() >= 9) return new ViaBossBar((BukkitTabPlayer) player);

        // 1.8- server and player, no implementation
        return new DummyBossBar();
    }

    @Override
    @NotNull
    @SneakyThrows
    public TabList createTabList(@NotNull TabPlayer player) {
        return tablistProvider.apply((BukkitTabPlayer) player);
    }

    @Override
    public boolean supportsNumberFormat() {
        return serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId();
    }

    @Override
    public boolean supportsListOrder() {
        return serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId();
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
     * This method may use reflections, because the return type changed in 1.8,
     * and we want to avoid errors.
     *
     * @return  Online players from Bukkit API.
     */
    @SneakyThrows
    @NotNull
    public Collection<? extends Player> getOnlinePlayers() {
        if (serverVersion.getMinorVersion() >= 8) {
            return Bukkit.getOnlinePlayers();
        }
        return Arrays.asList((Player[]) Bukkit.class.getMethod("getOnlinePlayers").invoke(null));
    }
}