package me.neznamy.tab.shared.platform.decorators;

import lombok.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Decorated class for TabList that tracks entries and their expected values.
 *
 * @param   <P>
 *          Platform's player class
 * @param   <C>
 *          Platform's component class
 */
@RequiredArgsConstructor
public abstract class TrackedTabList<P extends TabPlayer, C> implements TabList {

    /** Player this tablist belongs to */
    protected final P player;

    /** Tablist display name anti-override flag */
    @Setter
    @Getter
    private boolean antiOverride;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabPlayer, C> expectedDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    private final RedisSupport redisSupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<RedisPlayer, C> expectedRedisDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        C component = displayName == null ? null : toComponent(displayName);
        setExpectedDisplayName(entry, component);
        updateDisplayName(entry, component);
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        C component = entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName());
        setExpectedDisplayName(entry.getUniqueId(), component);
        addEntry(entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(), entry.getLatency(), entry.getGameMode(), component);

        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName(entry.getUniqueId(), component);
        }
    }

    /**
     * Checks if all entries have display names as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkDisplayNames() {
        // Empty by default, overridden by Sponge7, Sponge8 and Velocity
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     */
    public void onPacketSend(@NonNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    private void setExpectedDisplayName(@NonNull UUID entry, @Nullable C displayName) {
        if (!antiOverride) return;
        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(entry);
        if (player != null) expectedDisplayNames.put(player, displayName);

        if (redisSupport != null) {
            RedisPlayer redisPlayer = redisSupport.getRedisPlayers().get(entry);
            if (redisPlayer != null) expectedRedisDisplayNames.put(redisPlayer, displayName);
        }
    }

    /**
     * Returns expected display name for specified UUID. If nothing is found,
     * {@code null} is returned.
     *
     * @param   id
     *          UUID of tablist entry
     * @return  Expected display name or {@code null}
     */
    @Nullable
    public C getExpectedDisplayName(@NotNull UUID id) {
        if (!antiOverride) return null;

        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(id);
        if (player != null && expectedDisplayNames.containsKey(player)) {
            return expectedDisplayNames.get(player);
        }

        if (redisSupport != null) {
            RedisPlayer redisPlayer = redisSupport.getRedisPlayers().get(id);
            if (redisPlayer != null && expectedRedisDisplayNames.containsKey(redisPlayer)) {
                return expectedRedisDisplayNames.get(redisPlayer);
            }
        }
        return null;
    }

    @Nullable
    protected C getExpectedDisplayName(@NonNull TabPlayer player) {
        return expectedDisplayNames.get(player);
    }

    /**
     * Converts TAB component into platform's component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    public C toComponent(@NonNull TabComponent component) {
        return component.convert(player.getVersion());
    }

    /**
     * Updates display name of an entry. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead.
     *
     * @param   entry
     *          Entry to update
     * @param   displayName
     *          New display name
     */
    public abstract void updateDisplayName(@NonNull UUID entry, @Nullable C displayName);

    /**
     * Adds specified entry to tablist
     *
     * @param   id
     *          Entry UUID
     * @param   name
     *          Entry name
     * @param   skin
     *          Entry skin
     * @param   listed
     *          Whether entry should be listed or not
     * @param   latency
     *          Entry latency
     * @param   gameMode
     *          Entry game mode
     * @param   displayName
     *          Entry display name
     */
    public abstract void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin,
                  boolean listed, int latency, int gameMode, @Nullable C displayName);
}
