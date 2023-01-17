package me.neznamy.tab.platforms.bukkit;

import com.viaversion.viaversion.api.Via;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.WitherBossBar;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.BukkitNameTagX;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

/**
 * Implementation of Platform interface for Bukkit platform
 */
public class BukkitPlatform extends Platform {

    /** Plugin instance for registering tasks and events */
    private final JavaPlugin plugin;

    /** Variables checking presence of other plugins to hook into */
    private final boolean placeholderAPI = Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.PLACEHOLDER_API);
    private boolean libsDisguises = Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.LIBS_DISGUISES);
    private Plugin viaVersion;
    private final boolean protocolSupport = Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.PROTOCOL_SUPPORT);

    /**
     * Constructs new instance with given plugin parameter
     *
     * @param   plugin
     *          plugin instance
     */
    public BukkitPlatform(JavaPlugin plugin) {
        super(new BukkitPacketBuilder());
        this.plugin = plugin;
    }

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.LUCKPERMS)) {
            return new LuckPerms(getPluginVersion(TabConstants.Plugin.LUCKPERMS));
        } else if (Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.VAULT)) {
            RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
            if (provider == null) return new None();
            return new Vault(provider.getProvider(), getPluginVersion(TabConstants.Plugin.VAULT));
        } else {
            return new None();
        }
    }

    @Override
    public void loadFeatures() {
        if (Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.VIAVERSION)) {
            try {
                Class.forName("com.viaversion.viaversion.api.Via");
                viaVersion = Bukkit.getPluginManager().getPlugin(TabConstants.Plugin.VIAVERSION);
            } catch (ClassNotFoundException e) {
                TAB.getInstance().sendConsoleMessage("&cAn outdated version of ViaVersion (" + getPluginVersion(TabConstants.Plugin.VIAVERSION) + ") was detected.", true);
                TAB.getInstance().sendConsoleMessage("&cTAB only supports ViaVersion 4.0.0 and above. Disabling ViaVersion hook.", true);
                TAB.getInstance().sendConsoleMessage("&cThis might cause problems, such as limitations still being present for latest MC clients as well as RGB not working.", true);
            }
        }
        TAB tab = TAB.getInstance();
        if (tab.getConfiguration().isPipelineInjection())
            tab.getFeatureManager().registerFeature(TabConstants.Feature.PIPELINE_INJECTION, new BukkitPipelineInjector());
        new BukkitPlaceholderRegistry(plugin).registerPlaceholders(tab.getPlaceholderManager());
        if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true)) {
            tab.getFeatureManager().registerFeature(TabConstants.Feature.SORTING, new Sorting());
            if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false) && tab.getServerVersion().getMinorVersion() >= 8) {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, new BukkitNameTagX(plugin));
            } else {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());
            }
        }
        tab.loadUniversalFeatures();
        if (tab.getConfiguration().getConfig().getBoolean("bossbar.enabled", false)) {
            if (tab.getServerVersion().getMinorVersion() < 9) {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.BOSS_BAR, new WitherBossBar(plugin));
            } else {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
            }
        }
        if (tab.getServerVersion().getMinorVersion() >= 9 && tab.getConfiguration().getConfig().getBoolean("fix-pet-names.enabled", false))
            tab.getFeatureManager().registerFeature(TabConstants.Feature.PET_FIX, new PetFix());
        if (tab.getConfiguration().getConfig().getBoolean("per-world-playerlist.enabled", false))
            tab.getFeatureManager().registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, new PerWorldPlayerList(plugin));
        if (placeholderAPI && tab.getConfiguration().getConfig().getBoolean("placeholders.register-tab-expansion", true)) {
            BukkitTabExpansion expansion = new BukkitTabExpansion();
            expansion.register();
            TAB.getInstance().getPlaceholderManager().setTabExpansion(expansion);
        }
        for (Player p : getOnlinePlayers()) {
            tab.addPlayer(new BukkitTabPlayer(p, getProtocolVersion(p)));
        }
    }

    @Override
    public String getPluginVersion(String plugin) {
        Plugin pl = Bukkit.getPluginManager().getPlugin(plugin);
        return pl == null ? null : pl.getDescription().getVersion();
    }

    /**
     * Returns online players from Bukkit API
     *
     * @return  online players from Bukkit API
     */
    @SuppressWarnings("unchecked")
    private Player[] getOnlinePlayers() {
        try {
            Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
            if (players instanceof Player[]) {
                //1.7-
                return (Player[]) players;
            } else {
                //1.8+
                return ((Collection<Player>)players).toArray(new Player[0]); 
            }
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get online players", e);
            return new Player[0];
        }
    }

    @Override
    public void registerUnknownPlaceholder(String identifier) {
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        if (identifier.startsWith("%rel_")) {
            //relational placeholder
            TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, pl.getRelationalRefresh(identifier), (viewer, target) -> 
                placeholderAPI ? PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier) : identifier);
        } else {
            //normal placeholder
            if (identifier.startsWith("%sync:")) {
                int refresh = pl.getServerPlaceholderRefreshIntervals().getOrDefault(identifier,
                        pl.getPlayerPlaceholderRefreshIntervals().getOrDefault(identifier, pl.getDefaultRefresh()));
                String syncedPlaceholder = "%" + identifier.substring(6, identifier.length()-1) + "%";
                pl.registerPlaceholder(new PlayerPlaceholderImpl(identifier, refresh, null) {
                    
                    @Override
                    public Object request(TabPlayer p) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            long time = System.nanoTime();
                            updateValue(p, placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier);
                            TAB.getInstance().getCPUManager().addPlaceholderTime(getIdentifier(), System.nanoTime()-time);
                        });
                        return null;
                    }
                });
                return;
            }
            if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) {
                TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, pl.getServerPlaceholderRefreshIntervals().get(identifier), () ->
                        placeholderAPI ? PlaceholderAPI.setPlaceholders(null, identifier) : identifier);
            } else {
                int refresh = pl.getPlayerPlaceholderRefreshIntervals().getOrDefault(identifier, pl.getDefaultRefresh());
                TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh, p -> 
                    placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), identifier) : identifier);
            }
        }
    }

    /**
     * Returns status of LibsDisguises plugin presence
     *
     * @return  {@code true} if plugin is enabled, {@code false} if not
     */
    public boolean isLibsDisguisesEnabled() {
        return libsDisguises;
    }

    /**
     * Sets LibsDisguises presence status to provided value. This is used
     * to disable LibsDisguises hook in case the plugin is not correctly loaded
     * for any reason to avoid error spam in the hook.
     *
     * @param   enabled
     *          New status of LibsDisguises presence
     */
    public void setLibsDisguisesEnabled(boolean enabled) {
        libsDisguises = enabled;
    }

    /**
     * Gets protocol version of requested player and returns it.
     *
     * @param   player
     *          Player to get protocol version of
     * @return  protocol version of the player
     */
    public int getProtocolVersion(Player player) {
        if (protocolSupport){
            int version = getProtocolVersionPS(player);
            //some PS versions return -1 on unsupported server versions instead of throwing exception
            if (version != -1 && version < TAB.getInstance().getServerVersion().getNetworkId()) return version;
        }
        if (viaVersion != null) {
            return getProtocolVersionVia(player, 0);
        }
        return TAB.getInstance().getServerVersion().getNetworkId();
    }

    /**
     * Returns protocol version of requested player using ProtocolSupport
     *
     * @param   player
     *          Player to get protocol version of
     * @return  protocol version of the player using ProtocolSupport
     */
    private int getProtocolVersionPS(Player player){
        try {
            Object protocolVersion = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class).invoke(null, player);
            int version = (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
            TAB.getInstance().debug("ProtocolSupport returned protocol version " + version + " for " + player.getName() + " (online=" + player.isOnline() + ")");
            return version;
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError(String.format("Failed to get protocol version of %s using ProtocolSupport", player.getName()), e);
            return TAB.getInstance().getServerVersion().getNetworkId();
        }
    }

    /**
     * Returns protocol version of requested player using ViaVersion
     *
     * @param   player
     *          Player to get protocol version of
     * @return  protocol version of the player using ViaVersion
     */
    private int getProtocolVersionVia(Player player, int retryLevel){
        try {
            if (retryLevel == 10) {
                TAB.getInstance().debug("Failed to get protocol version of " + player.getName() + " after 10 retries");
                return TAB.getInstance().getServerVersion().getNetworkId();
            }
            int version = Via.getAPI().getPlayerVersion(player.getUniqueId());
            if (version == -1) {
                if (!player.isOnline()) return TAB.getInstance().getServerVersion().getNetworkId();
                Thread.sleep(5);
                return getProtocolVersionVia(player, retryLevel + 1);
            }
            TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + player.getName() + " (online=" + player.isOnline() + ")");
            return version;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } catch (Exception | LinkageError e) {
            TAB.getInstance().getErrorManager().printError(String.format("Failed to get protocol version of %s using ViaVersion v%s", player.getName(), viaVersion.getDescription().getVersion()), e);
            return TAB.getInstance().getServerVersion().getNetworkId();
        }
    }

    /**
     * Sends console message using ConsoleCommandSender, due to
     * Paper not translating colors correctly in Logger messages
     * and to allow RGB (at least on Paper, doesn't work on spigot)
     *
     * @param   message
     *          Message to send
     * @param   translateColors
     *          Whether color codes should be translated or not
     */
    @Override
    public void sendConsoleMessage(String message, boolean translateColors) {
        Bukkit.getConsoleSender().sendMessage("[TAB] " + (translateColors ?
                EnumChatFormat.color(RGBUtils.getInstance().convertToBukkitFormat(message,
                        TAB.getInstance().getServerVersion().getMinorVersion() >= 16))
                : message));
    }
}