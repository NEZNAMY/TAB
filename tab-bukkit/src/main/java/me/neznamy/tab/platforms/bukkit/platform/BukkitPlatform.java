package me.neznamy.tab.platforms.bukkit.platform;

import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.*;
import me.neznamy.tab.platforms.bukkit.entity.PacketEntityView;
import me.neznamy.tab.platforms.bukkit.hook.BukkitPremiumVanishHook;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.StructuredComponent;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.features.WitherBossBar;
import me.neznamy.tab.platforms.bukkit.features.BukkitNameTagX;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;

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

    /** NMS server to get TPS from on spigot */
    @Nullable
    private Object server;

    /** TPS field */
    @Nullable
    private Field spigotTps;

    /** Detection for presence of Paper's TPS getter */
    private final boolean paperTps = ReflectionUtils.methodExists(Bukkit.class, "getTPS");

    /** Detection for presence of Paper's MSPT getter */
    private final boolean paperMspt = ReflectionUtils.methodExists(Bukkit.class, "getAverageTickTime");

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
            server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            spigotTps = server.getClass().getField("recentTps");
        } catch (ReflectiveOperationException ignored) {
            //not spigot
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            PremiumVanishHook.setInstance(new BukkitPremiumVanishHook());
        }
        ComponentConverter.tryLoad();
        PacketEntityView.tryLoad();
        PingRetriever.tryLoad();
        TabListBase.findInstance();
        ScoreboardLoader.tryLoad();
        if (BukkitReflection.getMinorVersion() >= 8) {
            BukkitPipelineInjector.tryLoad();
        }
        BukkitUtils.sendCompatibilityMessage();
        Bukkit.getConsoleSender().sendMessage("[TAB] " + EnumChatFormat.GRAY + "Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms");
    }

    @Override
    @NotNull
    public BossBarManagerImpl getBossBar() {
        if (BukkitReflection.getMinorVersion() <= 8) return new WitherBossBar(plugin);
        return new BossBarManagerImpl();
    }

    @Override
    public void loadPlayers() {
        for (Player p : BukkitUtils.getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer(this, p));
        }
    }

    @Override
    public void registerPlaceholders() {
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        manager.registerServerPlaceholder("%vault-prefix%", -1, () -> "");
        manager.registerServerPlaceholder("%vault-suffix%", -1, () -> "");
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) {
                Chat chat = rspChat.getProvider();
                int refresh = TAB.getInstance().getConfiguration().getPermissionRefreshInterval();
                manager.registerPlayerPlaceholder("%vault-prefix%", refresh, p -> chat.getPlayerPrefix((Player) p.getPlayer()));
                manager.registerPlayerPlaceholder("%vault-suffix%", refresh, p -> chat.getPlayerSuffix((Player) p.getPlayer()));
            }
        }
        // Override for the PAPI placeholder to prevent console errors on unsupported server versions when ping field changes
        manager.registerPlayerPlaceholder("%player_ping%", manager.getRefreshInterval("%player_ping%"),
                p -> ((TabPlayer) p).getPing());
        BackendPlatform.super.registerPlaceholders();
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return BukkitReflection.getMinorVersion() >= 8 && BukkitPipelineInjector.isAvailable() ? new BukkitPipelineInjector() : null;
    }

    @Override
    @NotNull
    public NameTag getUnlimitedNameTags() {
        return BukkitReflection.getMinorVersion() >= 8 &&
                PacketEntityView.isAvailable() &&
                BukkitPipelineInjector.isAvailable() ?
                new BukkitNameTagX(plugin) : new NameTag();
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
    public TabFeature getPerWorldPlayerList() {
        return new PerWorldPlayerList(plugin);
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        if (!placeholderAPI) {
            registerDummyPlaceholder(identifier);
            return;
        }
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        int refresh = pl.getRefreshInterval(identifier);
        if (identifier.startsWith("%rel_")) {
            //relational placeholder
            TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, pl.getRefreshInterval(identifier), (viewer, target) ->
                    PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier));
        } else if (identifier.startsWith("%sync:")) {
            registerSyncPlaceholder(identifier, refresh);
        } else if (identifier.startsWith("%server_")) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, refresh,
                    () -> PlaceholderAPI.setPlaceholders(null, identifier));
        } else {
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh,
                    p -> PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), identifier));
        }
    }

    /**
     * Registers a sync placeholder with given identifier and refresh.
     *
     * @param   identifier
     *          Placeholder identifier
     * @param   refresh
     *          Placeholder refresh
     */
    public void registerSyncPlaceholder(@NotNull String identifier, int refresh) {
        String syncedPlaceholder = "%" + identifier.substring(6);
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh, p -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                long time = System.nanoTime();
                ppl[0].updateValue(p, placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier);
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, System.nanoTime() - time);
            });
            return null;
        });
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        Bukkit.getConsoleSender().sendMessage("[TAB] " + toBukkitFormat(message, true));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED + "[TAB] [WARN] " + toBukkitFormat(message, true));
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
        PluginCommand command = Bukkit.getPluginCommand(TabConstants.COMMAND_BACKEND);
        if (command != null) {
            BukkitTabCommand cmd = new BukkitTabCommand();
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        } else {
            logWarn(new SimpleComponent("Failed to register command, is it defined in plugin.yml?"));
        }
    }

    @Override
    public void startMetrics() {
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public Object convertComponent(@NotNull TabComponent component, boolean modern) {
        if (ComponentConverter.INSTANCE != null) {
            return ComponentConverter.INSTANCE.convert(component, modern);
        } else {
            return component;
        }
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
    @SneakyThrows
    public double getTPS() {
        if (paperTps) {
            return Bukkit.getTPS()[0];
        } else if (spigotTps != null) {
            return ((double[]) spigotTps.get(server))[0];
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
     * Runs an entity task that may or may not need to use its main thread.
     *
     * @param   entity
     *          Entity to work with
     * @param   task
     *          Task to run
     */
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        task.run();
    }

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        if (BackendPlatform.super.canSee(viewer, target)) return true;
        return ((BukkitTabPlayer)viewer).getPlayer().canSee(((BukkitTabPlayer)target).getPlayer());
    }

    /**
     * Converts component to legacy string using bukkit RGB format if supported by both server and client.
     * If not, closest legacy color is used instead.
     *
     * @param   component
     *          Component to convert
     * @param   rgbClient
     *          Whether client accepts RGB colors or not.
     * @return  Converted string using bukkit color format
     */
    @NotNull
    public String toBukkitFormat(@NotNull TabComponent component, boolean rgbClient) {
        if (component instanceof SimpleComponent) return component.toLegacyText();
        StructuredComponent iComponent = (StructuredComponent) component;
        StringBuilder sb = new StringBuilder();
        if (iComponent.getModifier().getColor() != null) {
            if (serverVersion.supportsRGB() && rgbClient) {
                String hexCode = iComponent.getModifier().getColor().getHexCode();
                char c = EnumChatFormat.COLOR_CHAR;
                sb.append(c).append("x").append(c).append(hexCode.charAt(0)).append(c).append(hexCode.charAt(1))
                        .append(c).append(hexCode.charAt(2)).append(c).append(hexCode.charAt(3))
                        .append(c).append(hexCode.charAt(4)).append(c).append(hexCode.charAt(5));
            } else {
                sb.append(iComponent.getModifier().getColor().getLegacyColor());
            }
        }
        sb.append(iComponent.getModifier().getMagicCodes());
        sb.append(iComponent.getText());
        for (StructuredComponent extra : iComponent.getExtra()) {
            sb.append(toBukkitFormat(extra, rgbClient));
        }
        return sb.toString();
    }
}