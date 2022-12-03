package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.nametags.unlimited.ProxyNameTagX;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class containing common variables and methods
 * shared between proxies.
 */
public abstract class ProxyPlatform extends Platform {

    /** Plugin message handler for sending and receiving plugin messages */
    protected final PluginMessageHandler plm = new PluginMessageHandler();

    /** Placeholders which are refreshed on backend server */
    private final Map<String, Integer> bridgePlaceholders = new ConcurrentHashMap<>();

    protected ProxyPlatform(PacketBuilder packetBuilder) {
        super(packetBuilder);
    }

    /**
     * Returns plugin message handler
     *
     * @return  plugin message handler
     */
    public PluginMessageHandler getPluginMessageHandler() {
        return plm;
    }

    /**
     * Returns bridge placeholders, which are refreshed on backend server
     *
     * @return  bridge placeholders, which are refreshed on backend server
     */
    public Map<String, Integer> getBridgePlaceholders() {
        return bridgePlaceholders;
    }

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
            return new VaultBridge();
        } else if (getPluginVersion("LuckPerms") != null) {
            return new LuckPerms(getPluginVersion("LuckPerms"));
        } else {
            return new VaultBridge();
        }
    }

    @Override
    public void registerUnknownPlaceholder(String identifier) {
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        //internal dynamic %online_<server>% placeholder
        if (identifier.startsWith("%online_")) {
            String server = identifier.substring(8, identifier.length()-1);
            pl.registerServerPlaceholder(identifier, 1000, () ->
                    Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> p.getServer().equals(server) && !p.isVanished()).count() +
                            getRedisPlayers().values().stream().filter(all -> all.getServer().equals(server) && !all.isVanished()).count());
            return;
        }
        Placeholder placeholder;
        int refresh;
        if (identifier.startsWith("%rel_")) {
            placeholder = pl.registerRelationalPlaceholder(identifier, -1, (viewer, target) -> null);
            refresh = pl.getRelationalRefresh(identifier);
        } else {
            placeholder = pl.registerPlayerPlaceholder(identifier, -1, player -> null);
            refresh = pl.getPlayerPlaceholderRefreshIntervals().getOrDefault(identifier,
                    pl.getServerPlaceholderRefreshIntervals().getOrDefault(identifier, pl.getDefaultRefresh()));
        }
        bridgePlaceholders.put(placeholder.getIdentifier(), refresh);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            plm.sendMessage(all, "Placeholder", placeholder.getIdentifier(), refresh);
        }
    }

    private Map<String, RedisPlayer> getRedisPlayers() {
        RedisSupport support = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        return support == null ? Collections.emptyMap() : support.getRedisPlayers();
    }

    @Override
    public void loadFeatures() {
        TAB tab = TAB.getInstance();
        new UniversalPlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
        if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true)) {
            tab.getFeatureManager().registerFeature(TabConstants.Feature.SORTING, new Sorting());
            if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, new ProxyNameTagX());
            } else {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());
            }
        }
        tab.loadUniversalFeatures();
        if (tab.getConfiguration().getConfig().getBoolean("bossbar.enabled", false))
            tab.getFeatureManager().registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
        if (tab.getConfiguration().getConfig().getBoolean("global-playerlist.enabled", false))
            tab.getFeatureManager().registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, new GlobalPlayerList());
        if (tab.getConfiguration().getConfig().getBoolean("fix-pet-names.enabled", false))
            tab.getFeatureManager().registerFeature(TabConstants.Feature.PET_FIX, new TabFeature("", "") {});
        if (tab.getConfiguration().getConfig().getBoolean("placeholders.register-tab-expansion", false)) {
            tab.getPlaceholderManager().setTabExpansion(new ProxyTabExpansion());
        }
    }
}
