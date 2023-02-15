package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.util.ReflectionUtils;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.WitherBossBar;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.BukkitNameTagX;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.TabExpansion;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
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
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Implementation of Platform interface for Bukkit platform
 */
public class BukkitPlatform extends BackendPlatform {

    @Getter private final BukkitPacketBuilder packetBuilder = new BukkitPacketBuilder();
    @Getter private final BukkitPipelineInjector pipelineInjector = new BukkitPipelineInjector();

    /** Plugin instance for registering tasks and events */
    private final JavaPlugin plugin;

    /** Variables checking presence of other plugins to hook into */
    private final boolean placeholderAPI = Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.PLACEHOLDER_API);
    @Getter @Setter private boolean libsDisguisesEnabled = Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.LIBS_DISGUISES);
    private Plugin viaVersion;
    private final boolean protocolSupport = Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.PROTOCOL_SUPPORT);

    /**
     * Constructs new instance with given plugin parameter
     *
     * @param   plugin
     *          plugin instance
     */
    public BukkitPlatform(JavaPlugin plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled(TabConstants.Plugin.VIAVERSION)) {
            if (ReflectionUtils.classExists("com.viaversion.viaversion.api.Via")) {
                viaVersion = Bukkit.getPluginManager().getPlugin(TabConstants.Plugin.VIAVERSION);
            } else {
                TAB.getInstance().sendConsoleMessage("&cAn outdated version of ViaVersion (" + getPluginVersion(TabConstants.Plugin.VIAVERSION) + ") was detected.", true);
                TAB.getInstance().sendConsoleMessage("&cTAB only supports ViaVersion 4.0.0 and above. Disabling ViaVersion hook.", true);
                TAB.getInstance().sendConsoleMessage("&cThis might cause problems, such as limitations still being present for latest MC clients as well as RGB not working.", true);
            }
        }
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

    public BossBarManagerImpl getLegacyBossBar() {
        return new WitherBossBar(plugin);
    }

    @Override
    public String getPluginVersion(String plugin) {
        Plugin pl = Bukkit.getPluginManager().getPlugin(plugin);
        return pl == null ? null : pl.getDescription().getVersion();
    }

    @Override
    public void loadPlayers() {
        for (Player p : getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer(p, getProtocolVersion(p)));
        }
    }

    @Override
    public void registerPlaceholders() {
        new BukkitPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new BukkitNameTagX(plugin);
    }

    @Override
    public TabExpansion getTabExpansion() {
        if (placeholderAPI) {
            BukkitTabExpansion expansion = new BukkitTabExpansion();
            expansion.register();
            return expansion;
        }
        return null;
    }

    @Override
    public TabFeature getPetFix() {
        return new PetFix();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerlist() {
        return new PerWorldPlayerList(plugin);
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
     * Gets protocol version of requested player and returns it.
     *
     * @param   player
     *          Player to get protocol version of
     * @return  protocol version of the player
     */
    public int getProtocolVersion(Player player) {
        if (protocolSupport) {
            int version = getProtocolVersionPS(player);
            //some PS versions return -1 on unsupported server versions instead of throwing exception
            if (version != -1 && version < TAB.getInstance().getServerVersion().getNetworkId()) return version;
        }
        if (viaVersion != null) {
            return getProtocolVersionVia(player.getUniqueId(), player.getName(), 0);
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
    private int getProtocolVersionPS(Player player) {
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