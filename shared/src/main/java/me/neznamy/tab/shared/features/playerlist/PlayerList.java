package me.neznamy.tab.shared.features.playerlist;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for TabList display names
 */
@Getter
public class PlayerList extends RefreshableFeature implements TabListFormatManager, JoinListener, Loadable,
        UnLoadable, WorldSwitchListener, ServerSwitchListener, VanishListener, RedisFeature, GroupListener {

    @NotNull private final StringToComponentCache cache = new StringToComponentCache("Tablist name formatting", 1000);
    @NotNull private final TablistFormattingConfiguration configuration;
    @Nullable private final RedisSupport redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
    @NotNull private final DisableChecker disableChecker;

    /**
     * Constructs new instance, registers disable checker into feature manager and starts anti-override.
     *
     * @param   configuration
     *          Feature configuration
     */
    public PlayerList(@NotNull TablistFormattingConfiguration configuration) {
        this.configuration = configuration;
        disableChecker = new DisableChecker(this, Condition.getCondition(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.tablistData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.PLAYER_LIST + "-Condition", disableChecker);
        if (configuration.isAntiOverride()) {
            TAB.getInstance().getCpu().getTablistEntryCheckThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                            ((TrackedTabList<?, ?>)p.getTabList()).checkDisplayNames();
                        }
                    }, getFeatureName(), CpuUsageCategory.ANTI_OVERRIDE_TABLIST_PERIODIC), 500
            );
        }
        if (redis != null) {
            redis.registerMessage("tabformat", UpdateRedisPlayer.class, UpdateRedisPlayer::new);
        }
    }

    /**
     * Returns UUID of tablist entry representing this player. If layout feature
     * is enabled, returns UUID of the layout slot where the player should be.
     * When it's not enabled, returns player's TabList UUID, which may not match
     * with player's actual UUID due to velocity.
     *
     * @param   p
     *          Player to get tablist UUID of
     * @param   viewer
     *          TabList viewer
     * @return  UUID of TabList entry representing requested player
     */
    @NotNull
    public UUID getTablistUUID(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        if (viewer.layoutData.currentLayout != null) {
            PlayerSlot slot = viewer.layoutData.currentLayout.view.getSlot(p);
            if (slot != null) {
                return slot.getUniqueId();
            }
        }
        return p.getTablistId(); //layout not enabled or player not visible to viewer
    }

    /**
     * Loads properties from config.
     *
     * @param   player
     *          Player to load properties for
     */
    public void loadProperties(@NotNull TabPlayer player) {
        player.tablistData.prefix = player.loadPropertyFromConfig(this, "tabprefix", "");
        player.tablistData.name = player.loadPropertyFromConfig(this, "customtabname", player.getName());
        player.tablistData.suffix = player.loadPropertyFromConfig(this, "tabsuffix", "");
    }

    /**
     * Loads all properties from config and returns {@code true} if at least
     * one of them either wasn't loaded or changed value, {@code false} otherwise.
     *
     * @param   p
     *          Player to update properties of
     * @return  {@code true} if at least one property changed, {@code false} if not
     */
    public boolean updateProperties(@NotNull TabPlayer p) {
        boolean changed = p.updatePropertyFromConfig(p.tablistData.prefix, "");
        if (p.updatePropertyFromConfig(p.tablistData.name, p.getName())) changed = true;
        if (p.updatePropertyFromConfig(p.tablistData.suffix, "")) changed = true;
        return changed;
    }

    /**
     * Updates TabList format of requested player to everyone.
     *
     * @param   player
     *          Player to update
     * @param   format
     *          Whether player's actual format should be used or {@code null} for reset
     */
    public void updatePlayer(@NotNull TabPlayer player, boolean format) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            //if (!viewer.getTabList().containsEntry(player.getTablistId())) continue;
            UUID tablistId = getTablistUUID(player, viewer);
            viewer.getTabList().updateDisplayName(tablistId, format ? getTabFormat(player, viewer) :
                    tablistId.getMostSignificantBits() == 0 ? new SimpleComponent(player.getName()) : null);
        }
        if (redis != null) redis.sendMessage(new UpdateRedisPlayer(player.getUniqueId(), player.tablistData.prefix.get() +
                player.tablistData.name.get() + player.tablistData.suffix.get()));
    }

    /**
     * Returns TabList format of player for viewer
     *
     * @param   p
     *          Player to get format of
     * @param   viewer
     *          Viewer seeing the format
     * @return  Format of specified player for viewer
     */
    @Nullable
    public TabComponent getTabFormat(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        Property prefix = p.tablistData.prefix;
        Property name = p.tablistData.name;
        Property suffix = p.tablistData.suffix;
        if (prefix == null || name == null || suffix == null) {
            return null;
        }
        return cache.get(prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer));
    }

    @Override
    public void load() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList<?, ?>)all.getTabList()).setAntiOverride(configuration.isAntiOverride());
            loadProperties(all);
            if (disableChecker.isDisableConditionMet(all)) {
                all.tablistData.disabled.set(true);
            } else {
                if (redis != null) redis.sendMessage(new UpdateRedisPlayer(all.getUniqueId(),
                        all.tablistData.prefix.get() + all.tablistData.name.get() + all.tablistData.suffix.get()));
            }
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (target.tablistData.disabled.get()) continue;
                //if (!viewer.getTabList().containsEntry(target.getTablistId())) continue;
                viewer.getTabList().updateDisplayName(getTablistUUID(target, viewer), getTabFormat(target, viewer));
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (target.tablistData.disabled.get()) continue;
                //if (!viewer.getTabList().containsEntry(target.getTablistId())) continue;
                viewer.getTabList().updateDisplayName(getTablistUUID(target, target), null);
            }
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        if (updateProperties(p) && !p.tablistData.disabled.get()) updatePlayer(p, true);
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
        TAB.getInstance().getCpu().getProcessingThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!all.tablistData.disabled.get() && p.getVersion().getMinorVersion() >= 8
                        //&& p.getTabList().containsEntry(all.getTablistId())
                )
                    p.getTabList().updateDisplayName(getTablistUUID(all, p), getTabFormat(all, p));
                if (all != p && !p.tablistData.disabled.get() && all.getVersion().getMinorVersion() >= 8
                        //&& all.getTabList().containsEntry(p.getTablistId())
                )
                    all.getTabList().updateDisplayName(getTablistUUID(p, all), getTabFormat(p, all));
            }
            if (redis != null) {
                for (RedisPlayer redis : redis.getRedisPlayers().values()) {
                    p.getTabList().updateDisplayName(redis.getUniqueId(), redis.getTabFormat());
                }
            }
        }, getFeatureName(), CpuUsageCategory.PLAYER_JOIN), 300);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        if (updateProperties(changed) && !changed.tablistData.disabled.get()) updatePlayer(changed, true);
    }

    /**
     * Processes disable condition change.
     *
     * @param   p
     *          Player who the condition has changed for
     * @param   disabledNow
     *          Whether the feature is disabled now or not
     */
    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        updatePlayer(p, !disabledNow);
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating TabList format";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.tablistData.prefix == null) return; // Placeholder in condition on join
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.tablistData.prefix.update();
            boolean name = refreshed.tablistData.name.update();
            boolean suffix = refreshed.tablistData.suffix.update();
            refresh = prefix || name || suffix;
        }
        if (refreshed.tablistData.disabled.get()) return;
        if (refresh) {
            updatePlayer(refreshed, true);
        }
    }

    @Override
    public void onGroupChange(@NotNull TabPlayer player) {
        if (updateProperties(player)) {
            updatePlayer(player, true);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        ((TrackedTabList<?, ?>)connectedPlayer.getTabList()).setAntiOverride(configuration.isAntiOverride());
        loadProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.tablistData.disabled.set(true);
        } else {
            updatePlayer(connectedPlayer, true);
        }
        if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
        Runnable r = () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == connectedPlayer) continue; // Already updated above
                connectedPlayer.getTabList().updateDisplayName(getTablistUUID(all, connectedPlayer), getTabFormat(all, connectedPlayer));
            }
            if (redis != null) {
                for (RedisPlayer redis : redis.getRedisPlayers().values()) {
                    connectedPlayer.getTabList().updateDisplayName(redis.getUniqueId(), redis.getTabFormat());
                }
            }
        };
        //add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
        if (!configuration.isAntiOverride() || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) {
            TAB.getInstance().getCpu().getProcessingThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(),
                    r, getFeatureName(), CpuUsageCategory.PLAYER_JOIN), 300);
        } else {
            r.run();
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished() || player.tablistData.disabled.get()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            //if (!viewer.getTabList().containsEntry(player.getTablistId())) continue;
            viewer.getTabList().updateDisplayName(player.getTablistId(), getTabFormat(player, viewer));
        }
    }

    // ------------------
    // API Implementation
    // ------------------
    
    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).tablistData.prefix.setTemporaryValue(prefix);
        updatePlayer(((TabPlayer)player), true);
    }

    @Override
    public void setName(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String customName) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).tablistData.name.setTemporaryValue(customName);
        updatePlayer(((TabPlayer)player), true);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).tablistData.suffix.setTemporaryValue(suffix);
        updatePlayer(((TabPlayer)player), true);
    }

    @Override
    public String getCustomPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.prefix.getTemporaryValue();
    }

    @Override
    public String getCustomName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.name.getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.suffix.getTemporaryValue();
    }

    @Override
    public @NotNull String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.prefix.getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.name.getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.suffix.getOriginalRawValue();
    }

    @Override
    public void onRedisLoadRequest() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            redis.sendMessage(new UpdateRedisPlayer(all.getTablistId(), all.tablistData.prefix.get() + all.tablistData.name.get() + all.tablistData.suffix.get()));
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull RedisPlayer player) {
        if (player.isVanished()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            viewer.getTabList().updateDisplayName(player.getUniqueId(), player.getTabFormat());
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Tablist name formatting";
    }

    /**
     * Class holding tablist formatting data for players.
     */
    public static class PlayerData {

        /** Player's tabprefix */
        public Property prefix;

        /** Player's customtabname */
        public Property name;

        /** Player's tabsuffix */
        public Property suffix;

        /** Flag tracking whether this feature is disabled for the player with condition or not */
        public final AtomicBoolean disabled = new AtomicBoolean();
    }

    /**
     * Redis message to update tablist format of a player.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    private class UpdateRedisPlayer extends RedisMessage {

        private UUID playerId;
        private String format;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(format);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            format = in.readUTF();
        }

        @Override
        public void process(@NotNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) {
                TAB.getInstance().getErrorManager().printError("Unable to process tablist format update of redis player " + playerId + ", because no such player exists", null);
                return;
            }
            if (target.getTabFormat() == null) {
                TAB.getInstance().debug("Processing tablist formatting join of redis player " + target.getName());
            }
            target.setTabFormat(cache.get(format));
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getVersion().getMinorVersion() < 8) continue;
                viewer.getTabList().updateDisplayName(target.getUniqueId(), target.getTabFormat());
            }
        }
    }
}