package me.neznamy.tab.shared.features.playerlist;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for TabList display names
 */
@Getter
public class PlayerList extends RefreshableFeature implements TabListFormatManager, JoinListener, Loadable,
        UnLoadable, WorldSwitchListener, ServerSwitchListener, VanishListener, ProxyFeature, GroupListener {

    @NotNull private final StringToComponentCache cache = new StringToComponentCache("Tablist name formatting", 1000);
    @NotNull private final TablistFormattingConfiguration configuration;
    @Nullable private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
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
        TAB.getInstance().getCpu().getTablistEntryCheckThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                ((TrackedTabList<?>)p.getTabList()).checkDisplayNames();
            }
        }, getFeatureName(), CpuUsageCategory.ANTI_OVERRIDE_TABLIST_PERIODIC), 500);
        if (proxy != null) {
            proxy.registerMessage(PlayerListUpdateProxyPlayer.class, in -> new PlayerListUpdateProxyPlayer(in, this));
        }
    }

    private void updateDisplayName(@NotNull TabPlayer viewer, @NotNull TabPlayer target, @Nullable TabComponent displayName) {
        if (viewer.layoutData.currentLayout != null) {
            PlayerSlot slot = viewer.layoutData.currentLayout.view.getSlot(target);
            if (slot != null) {
                viewer.getTabList().updateDisplayName(slot.getUniqueId(), displayName);
                return;
            }
        }
        viewer.getTabList().updateDisplayName(target, displayName);
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
            if (!viewer.canSee(player)) continue;
            // TODO This probably needs some layout check to make sure it does not use layout entry names for player names
            updateDisplayName(viewer, player, format ? getTabFormat(player, viewer) : null);
        }
        if (proxy != null) proxy.sendMessage(new PlayerListUpdateProxyPlayer(this, player.getUniqueId(), player.getName(), player.tablistData.prefix.get() +
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
            loadProperties(all);
            if (disableChecker.isDisableConditionMet(all)) {
                all.tablistData.disabled.set(true);
            } else {
                if (proxy != null) proxy.sendMessage(new PlayerListUpdateProxyPlayer(this, all.getUniqueId(), all.getName(),
                        all.tablistData.prefix.get() + all.tablistData.name.get() + all.tablistData.suffix.get()));
            }
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (target.tablistData.disabled.get()) continue;
                if (!viewer.canSee(target)) continue;
                updateDisplayName(viewer, target, getTabFormat(target, viewer));
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (target.tablistData.disabled.get()) continue;
                if (!viewer.canSee(target)) continue;
                updateDisplayName(viewer, target, null);
            }
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
        if (updateProperties(p) && !p.tablistData.disabled.get()) updatePlayer(p, true);
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
        TAB.getInstance().getCpu().getProcessingThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!all.tablistData.disabled.get() && p.canSee(all))
                    updateDisplayName(p, all, getTabFormat(all, p));
                if (all != p && !p.tablistData.disabled.get() && all.canSee(p))
                    updateDisplayName(all, p, getTabFormat(p, all));
            }
            if (proxy != null) {
                for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                    p.getTabList().updateDisplayName(proxied.getTablistId(), proxied.getTabFormat());
                }
            }
        }, getFeatureName(), CpuUsageCategory.PLAYER_JOIN), 300);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
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
        if (updateProperties(player) && !player.tablistData.disabled.get()) {
            updatePlayer(player, true);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        loadProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.tablistData.disabled.set(true);
        } else {
            updatePlayer(connectedPlayer, true);
        }
        Runnable r = () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == connectedPlayer) continue; // Already updated above
                if (all.tablistData.disabled.get()) continue;
                if (!connectedPlayer.canSee(all)) continue;
                updateDisplayName(connectedPlayer, all, getTabFormat(all, connectedPlayer));
            }
            if (proxy != null) {
                for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                    connectedPlayer.getTabList().updateDisplayName(proxied.getTablistId(), proxied.getTabFormat());
                }
            }
        };
        //add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
        if (!TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) {
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
            if (!viewer.canSee(player)) continue;
            viewer.getTabList().updateDisplayName(player, getTabFormat(player, viewer));
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
        if (((TabPlayer)player).tablistData.disabled.get()) return;
        updatePlayer(((TabPlayer)player), true);
    }

    @Override
    public void setName(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String customName) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).tablistData.name.setTemporaryValue(customName);
        if (((TabPlayer)player).tablistData.disabled.get()) return;
        updatePlayer(((TabPlayer)player), true);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        ((TabPlayer)player).tablistData.suffix.setTemporaryValue(suffix);
        if (((TabPlayer)player).tablistData.disabled.get()) return;
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
    @NotNull
    public String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawPrefix(player);
    }

    @Override
    @NotNull
    public String getOriginalName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawName(player);
    }

    @Override
    @NotNull
    public String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawSuffix(player);
    }

    @Override
    @NotNull
    public String getOriginalRawPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.prefix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalRawName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.name.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalRawSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.suffix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.prefix.getOriginalReplacedValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.name.getOriginalReplacedValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).tablistData.suffix.getOriginalReplacedValue();
    }

    // ------------------
    // ProxySupport
    // ------------------

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            proxy.sendMessage(new PlayerListUpdateProxyPlayer(this, all.getTablistId(), all.getName(), all.tablistData.prefix.get() + all.tablistData.name.get() + all.tablistData.suffix.get()));
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull ProxyPlayer player) {
        updatePlayer(player);
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        updatePlayer(player);
    }

    public void updatePlayer(@NotNull ProxyPlayer player) {
        if (player.isVanished()) return;
        if (player.getTabFormat() == null) return; // Player not loaded yet
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getTabList().updateDisplayName(player.getTablistId(), player.getTabFormat());
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
}