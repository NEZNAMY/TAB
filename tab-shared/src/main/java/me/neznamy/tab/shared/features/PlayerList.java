package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.LayoutView;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Feature handler for TabList display names
 */
@Getter
public class PlayerList extends TabFeature implements TabListFormatManager, JoinListener, Loadable,
        UnLoadable, WorldSwitchListener, ServerSwitchListener, Refreshable, VanishListener {

    /** Config option toggling anti-override which prevents other plugins from overriding TAB */
    protected final boolean antiOverrideTabList = config().getBoolean("tablist-name-formatting.anti-override", true);

    private final LayoutManagerImpl layoutManager = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
    private RedisSupport redis;
    protected final DisableChecker disableChecker;

    /**
     * Flag tracking when the plugin is disabling to properly clear
     * display name by setting it to null value and not force the value back
     * with the anti-override.
     */
    private boolean disabling;

    /**
     * Constructs new instance, registers disable checker into feature manager and starts anti-override.
     */
    public PlayerList() {
        Condition disableCondition = Condition.getCondition(config().getString("tablist-name-formatting.disable-condition"));
        disableChecker = new DisableChecker(getFeatureName(), disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.PLAYER_LIST + "-Condition", disableChecker);
        if (antiOverrideTabList) {
            TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, getFeatureName(), TabConstants.CpuUsageCategory.ANTI_OVERRIDE, () -> {
                for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                    p.getTabList().checkDisplayNames();
                }
            });
        } else {
            TAB.getInstance().getConfigHelper().startup().tablistAntiOverrideDisabled();
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
    public UUID getTablistUUID(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        if (layoutManager != null) {
            LayoutView layout = layoutManager.getViews().get(viewer);
            if (layout != null) {
                PlayerSlot slot = layout.getSlot(p);
                if (slot != null) {
                    return slot.getUniqueId();
                }
            }
        }
        return p.getTablistId(); //layout not enabled or player not visible to viewer
    }

    /**
     * Loads all properties from config and returns {@code true} if at least
     * one of them either wasn't loaded or changed value, {@code false} otherwise.
     *
     * @param   p
     *          Player to update properties of
     * @return  {@code true} if at least one property changed, {@code false} if not
     */
    protected boolean updateProperties(@NotNull TabPlayer p) {
        boolean changed = p.loadPropertyFromConfig(this, TabConstants.Property.TABPREFIX);
        if (p.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTABNAME, p.getName())) changed = true;
        if (p.loadPropertyFromConfig(this, TabConstants.Property.TABSUFFIX)) changed = true;
        return changed;
    }

    /**
     * Updates TabList format of requested player to everyone.
     *
     * @param   p
     *          Player to update
     * @param   format
     *          Whether player's actual format should be used or {@code null} for reset
     */
    protected void updatePlayer(@NotNull me.neznamy.tab.api.TabPlayer p, boolean format) {
        TabPlayer player = (TabPlayer) p;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            if (!viewer.getTabList().containsEntry(player.getTablistId())) continue;
            UUID tablistId = getTablistUUID(player, viewer);
            viewer.getTabList().updateDisplayName(tablistId, format ? getTabFormat(player, viewer) :
                    tablistId.getMostSignificantBits() == 0 ? new SimpleComponent(player.getName()) : null);
        }
        if (redis != null) redis.updateTabFormat(player, player.getProperty(TabConstants.Property.TABPREFIX).get() +
                player.getProperty(TabConstants.Property.CUSTOMTABNAME).get() + player.getProperty(TabConstants.Property.TABSUFFIX).get());
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
    public @Nullable TabComponent getTabFormat(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        Property prefix = p.getProperty(TabConstants.Property.TABPREFIX);
        Property name = p.getProperty(TabConstants.Property.CUSTOMTABNAME);
        Property suffix = p.getProperty(TabConstants.Property.TABSUFFIX);
        if (prefix == null || name == null || suffix == null) {
            return null;
        }
        return TabComponent.optimized(prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer));
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            all.getTabList().setAntiOverride(antiOverrideTabList);
            updateProperties(all);
            if (disableChecker.isDisableConditionMet(all)) {
                disableChecker.addDisabledPlayer(all);
            } else {
                if (redis != null) redis.updateTabFormat(all, all.getProperty(TabConstants.Property.TABPREFIX).get() + all.getProperty(TabConstants.Property.CUSTOMTABNAME).get() + all.getProperty(TabConstants.Property.TABSUFFIX).get());
            }
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (disableChecker.isDisabledPlayer(target)) continue;
                if (!viewer.getTabList().containsEntry(target.getTablistId())) continue;
                viewer.getTabList().updateDisplayName(getTablistUUID(target, viewer), getTabFormat(target, viewer));
            }
        }
    }

    @Override
    public void unload() {
        disabling = true;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (disableChecker.isDisabledPlayer(target)) continue;
                if (!viewer.getTabList().containsEntry(target.getTablistId())) continue;
                viewer.getTabList().updateDisplayName(getTablistUUID(target, target), null);
            }
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        if (updateProperties(p) && !disableChecker.isDisabledPlayer(p)) updatePlayer(p, true);
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
        TAB.getInstance().getCPUManager().runTaskLater(300, getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!disableChecker.isDisabledPlayer(all) && p.getVersion().getMinorVersion() >= 8
                        && p.getTabList().containsEntry(all.getTablistId()))
                    p.getTabList().updateDisplayName(getTablistUUID(all, p), getTabFormat(all, p));
                if (all != p && !disableChecker.isDisabledPlayer(p) && all.getVersion().getMinorVersion() >= 8
                        && all.getTabList().containsEntry(p.getTablistId()))
                    all.getTabList().updateDisplayName(getTablistUUID(p, all), getTabFormat(p, all));
            }
        });
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        if (updateProperties(changed) && !disableChecker.isDisabledPlayer(changed)) updatePlayer(changed, true);
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

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.getProperty(TabConstants.Property.TABPREFIX) == null) return; // Placeholder in condition on join
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.getProperty(TabConstants.Property.TABPREFIX).update();
            boolean name = refreshed.getProperty(TabConstants.Property.CUSTOMTABNAME).update();
            boolean suffix = refreshed.getProperty(TabConstants.Property.TABSUFFIX).update();
            refresh = prefix || name || suffix;
        }
        if (disableChecker.isDisabledPlayer(refreshed)) return;
        if (refresh) {
            updatePlayer(refreshed, true);
        }
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating TabList format";
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.getTabList().setAntiOverride(antiOverrideTabList);
        updateProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
            return;
        }
        Runnable r = () -> {
            refresh(connectedPlayer, true);
            if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                connectedPlayer.getTabList().updateDisplayName(getTablistUUID(all, connectedPlayer), getTabFormat(all, connectedPlayer));
            }
        };
        //add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
        if (!antiOverrideTabList || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) {
            TAB.getInstance().getCPUManager().runTaskLater(300, getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN, r);
        } else {
            r.run();
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished() || disableChecker.isDisabledPlayer(player)) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            if (!viewer.getTabList().containsEntry(player.getTablistId())) continue;
            viewer.getTabList().updateDisplayName(player.getTablistId(), getTabFormat(player, viewer));
        }
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "Tablist name formatting";
    }

    // ------------------
    // API Implementation
    // ------------------
    
    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).getProperty(TabConstants.Property.TABPREFIX).setTemporaryValue(prefix);
        updatePlayer(player, true);
    }

    @Override
    public void setName(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String customName) {
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTABNAME).setTemporaryValue(customName);
        updatePlayer(player, true);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).getProperty(TabConstants.Property.TABSUFFIX).setTemporaryValue(suffix);
        updatePlayer(player, true);
    }

    @Override
    public String getCustomPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTABNAME).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABSUFFIX).getTemporaryValue();
    }

    @Override
    public @NotNull String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABPREFIX).getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTABNAME).getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABSUFFIX).getOriginalRawValue();
    }
}