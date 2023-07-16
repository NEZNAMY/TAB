package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.LayoutView;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Feature handler for TabList display names
 */
public class PlayerList extends TabFeature implements TabListFormatManager, JoinListener, DisplayNameListener, Loadable,
        UnLoadable, WorldSwitchListener, ServerSwitchListener, Refreshable, VanishListener {

    @Getter protected final String featureName = "Tablist name formatting";
    @Getter private final String refreshDisplayName = "Updating TabList format";

    /** Config option toggling anti-override which prevents other plugins from overriding TAB */
    @Getter protected final boolean antiOverrideTabList = TAB.getInstance().getConfiguration().getConfig().getBoolean("tablist-name-formatting.anti-override", true);

    private final LayoutManagerImpl layoutManager = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
    private RedisSupport redis;
    @Getter protected final DisableChecker disableChecker;

    /**
     * Flag tracking when the plugin is disabling to properly clear
     * display name by setting it to null value and not force the value back
     * with the anti-override.
     */
    private boolean disabling = false;

    public PlayerList() {
        Condition disableCondition = Condition.getCondition(TAB.getInstance().getConfig().getString("tablist-name-formatting.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.PLAYER_LIST + "-Condition", disableChecker);
        if (!antiOverrideTabList) TAB.getInstance().getMisconfigurationHelper().tablistAntiOverrideDisabled();
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
            UUID tablistId = getTablistUUID(player, viewer);
            viewer.getTabList().updateDisplayName(tablistId, format ? getTabFormat(player, viewer) :
                    tablistId.getMostSignificantBits() == 0 ? new IChatBaseComponent(player.getName()) : null);
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
    public @Nullable IChatBaseComponent getTabFormat(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        Property prefix = p.getProperty(TabConstants.Property.TABPREFIX);
        Property name = p.getProperty(TabConstants.Property.CUSTOMTABNAME);
        Property suffix = p.getProperty(TabConstants.Property.TABSUFFIX);
        if (prefix == null || name == null || suffix == null) {
            return null;
        }
        return IChatBaseComponent.optimizedComponent(prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer));
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
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
                if (!disableChecker.isDisabledPlayer(target)) viewer.getTabList().updateDisplayName(getTablistUUID(target, target), null);
            }
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        if (updateProperties(p) && !disableChecker.isDisabledPlayer(p)) updatePlayer(p, true);
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
        TAB.getInstance().getCPUManager().runTaskLater(300, featureName, TabConstants.CpuUsageCategory.PLAYER_JOIN, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!disableChecker.isDisabledPlayer(all) && p.getVersion().getMinorVersion() >= 8)
                    p.getTabList().updateDisplayName(getTablistUUID(all, p), getTabFormat(all, p));
                if (all != p && !disableChecker.isDisabledPlayer(p) && all.getVersion().getMinorVersion() >= 8)
                    all.getTabList().updateDisplayName(getTablistUUID(p, all), getTabFormat(p, all));
            }
        });
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        if (updateProperties(changed) && !disableChecker.isDisabledPlayer(changed)) updatePlayer(changed, true);
    }

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
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        updateProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
            return;
        }
        Runnable r = () -> {
            refresh(connectedPlayer, true);
            if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == connectedPlayer) continue; //already sent 4 lines above
                connectedPlayer.getTabList().updateDisplayName(getTablistUUID(all, connectedPlayer), getTabFormat(all, connectedPlayer));
            }
        };
        //add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
        if (!antiOverrideTabList || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION) ||
                connectedPlayer.getVersion().getMinorVersion() == 8) {
            TAB.getInstance().getCPUManager().runTaskLater(300, featureName, TabConstants.CpuUsageCategory.PLAYER_JOIN, r);
        } else {
            r.run();
        }
    }

    @Override
    public IChatBaseComponent onDisplayNameChange(@NotNull TabPlayer packetReceiver, @NotNull UUID id) {
        if (disabling || !antiOverrideTabList) return null;
        TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(id);
        if (packetPlayer != null && !disableChecker.isDisabledPlayer(packetPlayer) && packetPlayer.getTablistId() == getTablistUUID(packetPlayer, packetReceiver)) {
            return getTabFormat(packetPlayer, packetReceiver);
        }
        return null;
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getTabList().updateDisplayName(player.getTablistId(), getTabFormat(player, viewer));
        }
    }

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        Preconditions.checkLoaded(player);
        ((TabPlayer)player).getProperty(TabConstants.Property.TABPREFIX).setTemporaryValue(prefix);
        updatePlayer(player, true);
    }

    @Override
    public void setName(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String customName) {
        Preconditions.checkLoaded(player);
        ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTABNAME).setTemporaryValue(customName);
        updatePlayer(player, true);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        Preconditions.checkLoaded(player);
        ((TabPlayer)player).getProperty(TabConstants.Property.TABSUFFIX).setTemporaryValue(suffix);
        updatePlayer(player, true);
    }

    @Override
    public String getCustomPrefix(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomName(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTABNAME).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABSUFFIX).getTemporaryValue();
    }

    @Override
    public @NotNull String getOriginalPrefix(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABPREFIX).getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalName(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTABNAME).getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalSuffix(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TABSUFFIX).getOriginalRawValue();
    }
}