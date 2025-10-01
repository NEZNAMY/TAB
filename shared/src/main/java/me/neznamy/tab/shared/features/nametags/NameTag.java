package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.config.MessageFile;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class NameTag extends RefreshableFeature implements NameTagManager, JoinListener, QuitListener,
        Loadable, WorldSwitchListener, ServerSwitchListener, VanishListener, CustomThreaded, ProxyFeature, GroupListener {

    private final ThreadExecutor customThread = new ThreadExecutor("TAB NameTag Thread");
    private OnlinePlayers onlinePlayers;
    private final TeamConfiguration configuration;
    private final StringToComponentCache prefixCache = new StringToComponentCache("NameTag prefix", 1000);
    private final StringToComponentCache suffixCache = new StringToComponentCache("NameTag suffix", 1000);
    private final VisibilityRefresher visibilityRefresher;
    private final CollisionManager collisionManager;
    private final int teamOptions;
    private final DisableChecker disableChecker;
    @Nullable private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);

    /**
     * Constructs new instance and registers sub-features.
     *
     * @param   configuration
     *          Feature configuration
     */
    public NameTag(@NotNull TeamConfiguration configuration) {
        this.configuration = configuration;
        teamOptions = configuration.isCanSeeFriendlyInvisibles() ? 2 : 0;
        disableChecker = new DisableChecker(this, TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.teamData.disabled);
        visibilityRefresher = new VisibilityRefresher(this);
        collisionManager = new CollisionManager(this);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS + "-Condition", disableChecker);
        if (proxy != null) {
            proxy.registerMessage(NameTagProxyPlayerData.class, in -> new NameTagProxyPlayerData(in, this));
        }
    }

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, visibilityRefresher);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        visibilityRefresher.load();
        collisionManager.load();
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            loadProperties(all);
            all.teamData.teamName = all.sortingData.shortTeamName; // Sorting is loaded sync before nametags
            if (disableChecker.isDisableConditionMet(all)) {
                all.teamData.disabled.set(true);
                continue;
            }
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(all, true);
            sendProxyMessage(all);
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (TabPlayer target : onlinePlayers.getPlayers()) {
                if (target.isVanished() && !viewer.canSee(target)) {
                    target.teamData.vanishedFor.add(viewer.getUniqueId());
                }
                if (!target.teamData.isDisabled()) registerTeam(target, viewer);
            }
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating prefix/suffix";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.teamData.isDisabled()) return;
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.teamData.prefix.update();
            boolean suffix = refreshed.teamData.suffix.update();
            refresh = prefix || suffix;
        }
        if (refresh) updatePrefixSuffix(refreshed);
    }

    @Override
    public void onGroupChange(@NotNull TabPlayer player) {
        if (updateProperties(player) && !player.teamData.isDisabled()) {
            updatePrefixSuffix(player);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        loadProperties(connectedPlayer);
        connectedPlayer.teamData.teamName = connectedPlayer.sortingData.shortTeamName; // Sorting is loaded sync before nametags
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            if (all == connectedPlayer) continue; //avoiding double registration
            if (connectedPlayer.isVanished() && !all.canSee(connectedPlayer)) {
                connectedPlayer.teamData.vanishedFor.add(all.getUniqueId());
            }
            if (all.isVanished() && !connectedPlayer.canSee(all)) {
                all.teamData.vanishedFor.add(connectedPlayer.getUniqueId());
            }
            if (!all.teamData.isDisabled()) {
                registerTeam(all, connectedPlayer);
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(connectedPlayer, true);
        if (proxy != null) {
            ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(connectedPlayer.getUniqueId());
            if (proxyPlayer != null && proxyPlayer.getNametag() != null) {
                for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                    ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(proxyPlayer.getNametag().getResolvedTeamName());
                }
                proxyPlayer.setNametag(null);
            }
        }
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.teamData.disabled.set(true);
            return;
        }
        registerTeam(connectedPlayer);
        if (proxy != null) {
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.getNametag() == null) continue; // This proxy player is not loaded yet
                TabComponent prefix = prefixCache.get(proxied.getNametag().getPrefix());
                TabComponent suffix = suffixCache.get(proxied.getNametag().getSuffix());
                connectedPlayer.getScoreboard().registerTeam(
                        proxied.getNametag().getResolvedTeamName(),
                        prefix,
                        suffix,
                        proxied.getNametag().getNameVisibility(),
                        CollisionRule.ALWAYS,
                        Collections.singletonList(proxied.getNickname()),
                        teamOptions,
                        prefix.getLastStyle().toEnumChatFormat()
                );
            }
            sendProxyMessage(connectedPlayer);
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(disconnectedPlayer.teamData.teamName);
        }
    }

    @Override
    public void onServerChange(@NonNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
        if (updateProperties(p) && !p.teamData.isDisabled()) updatePrefixSuffix(p);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
        if (updateProperties(changed) && !changed.teamData.isDisabled()) updatePrefixSuffix(changed);
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished()) {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (viewer == player) continue;
                if (!viewer.canSee(player)) {
                    player.teamData.vanishedFor.add(viewer.getUniqueId());
                    if (!player.teamData.isDisabled()) {
                        ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(player.teamData.teamName);
                    }
                }
            }
        } else {
            Set<UUID> ids = new HashSet<>(player.teamData.vanishedFor);
            player.teamData.vanishedFor.clear();
            if (!player.teamData.isDisabled()) {
                for (UUID id : ids) {
                    TabPlayer viewer = TAB.getInstance().getPlayer(id);
                    if (viewer != null) registerTeam(player, viewer);
                }
            }
        }
    }

    /**
     * Loads properties from config.
     *
     * @param   player
     *          Player to load properties for
     */
    private void loadProperties(@NotNull TabPlayer player) {
        player.teamData.prefix = player.loadPropertyFromConfig(this, "tagprefix", "");
        player.teamData.suffix = player.loadPropertyFromConfig(this, "tagsuffix", "");
    }

    /**
     * Loads all properties from config and returns {@code true} if at least
     * one of them either wasn't loaded or changed value, {@code false} otherwise.
     *
     * @param   p
     *          Player to update properties of
     * @return  {@code true} if at least one property changed, {@code false} if not
     */
    private boolean updateProperties(@NotNull TabPlayer p) {
        boolean changed = p.updatePropertyFromConfig(p.teamData.prefix, "");
        if (p.updatePropertyFromConfig(p.teamData.suffix, "")) changed = true;
        return changed;
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            unregisterTeam(p.teamData.teamName);
        } else {
            registerTeam(p);
        }
    }

    /**
     * Updates team prefix and suffix of given player.
     *
     * @param   player
     *          Player to update prefix/suffix of
     */
    private void updatePrefixSuffix(@NonNull TabPlayer player) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            TabComponent prefix = prefixCache.get(player.teamData.prefix.getFormat(viewer));
            viewer.getScoreboard().updateTeam(
                    player.teamData.teamName,
                    prefix,
                    suffixCache.get(player.teamData.suffix.getFormat(viewer)),
                    prefix.getLastStyle().toEnumChatFormat()
            );
        }
        sendProxyMessage(player);
    }

    /**
     * Updates collision of a player for everyone.
     *
     * @param   player
     *          Player to update collision of
     * @param   moveToThread
     *          Whether task should be moved to feature thread or not, because it already is
     */
    public void updateCollision(@NonNull TabPlayer player, boolean moveToThread) {
        Runnable r = () -> {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                viewer.getScoreboard().updateTeam(
                        player.teamData.teamName,
                        player.teamData.getCollisionRule() ? CollisionRule.ALWAYS : CollisionRule.NEVER
                );
            }
        };
        if (moveToThread) {
            customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), r, getFeatureName(), "Updating collision"));
        } else {
            r.run();
        }
    }

    /**
     * Updates visibility of a player for everyone.
     *
     * @param   player
     *          Player to update visibility of
     */
    public void updateVisibility(@NonNull TabPlayer player) {
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                viewer.getScoreboard().updateTeam(
                        player.teamData.teamName,
                        getTeamVisibility(player, viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER
                );
            }
            sendProxyMessage(player);
        }, getFeatureName(), "Updating visibility"));
    }

    /**
     * Updates visibility of a player for specified player.
     *
     * @param   player
     *          Player to update visibility of
     * @param   viewer
     *          Viewer to send update to
     */
    public void updateVisibility(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
        viewer.getScoreboard().updateTeam(
                player.teamData.teamName,
                getTeamVisibility(player, viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER
        );
    }

    private void unregisterTeam(@NonNull String teamName) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(teamName);
        }
    }

    private void registerTeam(@NonNull TabPlayer p) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            registerTeam(p, viewer);
        }
    }

    private void registerTeam(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (p.teamData.isDisabled() || p.teamData.vanishedFor.contains(viewer.getUniqueId())) return;
        if (!viewer.canSee(p) && p != viewer) return;
        TabComponent prefix = prefixCache.get(p.teamData.prefix.getFormat(viewer));
        viewer.getScoreboard().registerTeam(
                p.teamData.teamName,
                prefix,
                suffixCache.get(p.teamData.suffix.getFormat(viewer)),
                getTeamVisibility(p, viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                p.teamData.getCollisionRule() ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                Collections.singletonList(p.getNickname()),
                teamOptions,
                prefix.getLastStyle().toEnumChatFormat()
        );
    }

    public boolean getTeamVisibility(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (p.teamData.hasHiddenNametag()) return false; // At least 1 reason for invisible nametag exists
        if (p.teamData.hasHiddenNametag(viewer)) return false; // At least 1 reason for invisible nametag for this viewer exists
        if (viewer.teamData.invisibleNameTagView) return false; // Viewer does not want to see nametags
        if (viewer.getVersion().getMinorVersion() == 8 && p.hasInvisibilityPotion()) return false;
        return true;
    }

    /**
     * Updates team name for a specified player to everyone.
     *
     * @param   player
     *          Player to change team name of
     * @param   newTeamName
     *          New team name to use
     */
    public void updateTeamName(@NonNull TabPlayer player, @NonNull String newTeamName) {
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            // Function ran before onJoin did (super rare), drop action since onJoin will use new team name anyway
            if (player.teamData.teamName == null) return;

            if (player.teamData.isDisabled()) {
                player.teamData.teamName = newTeamName;
                return;
            }
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                viewer.getScoreboard().renameTeam(player.teamData.teamName, newTeamName);
            }
            player.teamData.teamName = newTeamName;
            sendProxyMessage(player);
        }, getFeatureName(), "Updating team name"));
    }

    public void hideNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason,
                            boolean sendMessage) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (player.teamData.hideNametag(reason)) {
                updateVisibility(player);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
        }, getFeatureName(), cpuReason));
    }

    public void hideNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason,
                            @NonNull String cpuReason, boolean sendMessage) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (player.teamData.hideNametag(viewer, reason)) {
                updateVisibility(player, viewer);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
        }, getFeatureName(), cpuReason));
    }

    public void showNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason,
                            boolean sendMessage) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (player.teamData.showNametag(reason)) {
                updateVisibility(player);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
        }, getFeatureName(), cpuReason));
    }

    public void showNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason,
                            @NonNull String cpuReason, boolean sendMessage) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (player.teamData.showNametag(viewer, reason)) {
                updateVisibility(player, viewer);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
        }, getFeatureName(), cpuReason));
    }

    public void toggleNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason,
                              boolean sendMessage) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (player.teamData.hasHiddenNametag(reason)) {
                player.teamData.showNametag(reason);
                if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
            } else {
                player.teamData.hideNametag(reason);
                if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
            }
            updateVisibility(player);
        }, getFeatureName(), cpuReason));
    }

    public void toggleNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason,
                              @NonNull String cpuReason, boolean sendMessage) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (player.teamData.hasHiddenNametag(viewer, reason)) {
                player.teamData.showNametag(viewer, reason);
                if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
            } else {
                player.teamData.hideNametag(viewer, reason);
                if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
            }
            updateVisibility(player, viewer);
        }, getFeatureName(), cpuReason));
    }

    // ------------------
    // ProxySupport
    // ------------------

    private void sendProxyMessage(@NotNull TabPlayer player) {
        if (proxy != null) {
            proxy.sendMessage(new NameTagProxyPlayerData(
                    this,
                    proxy.getIdCounter().incrementAndGet(),
                    player.getUniqueId(),
                    player.teamData.teamName,
                    player.teamData.prefix.get(),
                    player.teamData.suffix.get(),
                    getTeamVisibility(player, player) ? NameVisibility.ALWAYS : NameVisibility.NEVER
            ));
        }
    }

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            sendProxyMessage(all);
        }
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        if (player.getNametag() == null) {
            // One of the two options is being forcibly unregistered when real player joined
            return;
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(player.getNametag().getResolvedTeamName());
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        if (player.getNametag() == null) return; // Player not loaded yet
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            TabComponent prefix = prefixCache.get(player.getNametag().getPrefix());
            viewer.getScoreboard().registerTeam(
                    player.getNametag().getResolvedTeamName(),
                    prefix,
                    suffixCache.get(player.getNametag().getSuffix()),
                    player.getNametag().getNameVisibility(),
                    Scoreboard.CollisionRule.ALWAYS,
                    Collections.singletonList(player.getNickname()),
                    teamOptions,
                    prefix.getLastStyle().toEnumChatFormat()
            );
        }
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        hideNameTag((TabPlayer) player, NameTagInvisibilityReason.API_HIDE, "Processing API call (hideNameTag)", false);
    }

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        hideNameTag((TabPlayer) player, (TabPlayer) viewer, NameTagInvisibilityReason.API_HIDE, "Processing API call (hideNameTag)", false);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        showNameTag((TabPlayer) player, NameTagInvisibilityReason.API_HIDE, "Processing API call (showNameTag)", false);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        showNameTag((TabPlayer) player, (TabPlayer) viewer, NameTagInvisibilityReason.API_HIDE, "Processing API call (showNameTag)", false);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).teamData.hasHiddenNametag(NameTagInvisibilityReason.API_HIDE);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        ensureActive();
        return ((TabPlayer)player).teamData.hasHiddenNametag((TabPlayer) viewer, NameTagInvisibilityReason.API_HIDE);
    }

    @Override
    public void pauseTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (p.teamData.teamHandlingPaused) return;
            if (!p.teamData.isDisabled()) unregisterTeam(p.teamData.teamName);
            p.teamData.teamHandlingPaused = true; //setting after, so unregisterTeam method runs
        }, getFeatureName(), "Processing API call (pauseTeamHandling)"));
    }

    @Override
    public void resumeTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (!p.teamData.teamHandlingPaused) return;
            p.teamData.teamHandlingPaused = false; //setting before, so registerTeam method runs
            if (!p.teamData.isDisabled()) registerTeam(p);
        }, getFeatureName(), "Processing API call (resumeTeamHandling)"));
    }

    @Override
    public boolean hasTeamHandlingPaused(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return ((TabPlayer)player).teamData.teamHandlingPaused;
    }

    @Override
    public void setCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player, Boolean collision) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (Objects.equals(p.teamData.forcedCollision, collision)) return;
            p.teamData.forcedCollision = collision;
            updateCollision(p, true);
        }, getFeatureName(), "Processing API call (setCollisionRule)"));
    }

    @Override
    public Boolean getCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.forcedCollision;
    }

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            p.teamData.prefix.setTemporaryValue(prefix);
            updatePrefixSuffix(p);
        }, getFeatureName(), "Processing API call (setPrefix)"));
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            p.teamData.suffix.setTemporaryValue(suffix);
            updatePrefixSuffix(p);
        }, getFeatureName(), "Processing API call (setSuffix)"));
    }

    @Override
    public String getCustomPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getTemporaryValue();
    }

    @Override
    @NotNull
    public String getOriginalRawPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalRawSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getOriginalReplacedValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getOriginalReplacedValue();
    }

    @Override
    @NotNull
    public String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawPrefix(player);
    }

    @Override
    @NotNull
    public String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawSuffix(player);
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        setNameTagVisibilityView((TabPlayer) p, ((TabPlayer) p).teamData.invisibleNameTagView, sendToggleMessage);
    }

    @Override
    public void showNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        setNameTagVisibilityView((TabPlayer) p, true, sendToggleMessage);
    }

    @Override
    public void hideNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        setNameTagVisibilityView((TabPlayer) p, false, sendToggleMessage);
    }

    private void setNameTagVisibilityView(@NonNull TabPlayer player, boolean visible, boolean sendToggleMessage) {
        ensureActive();
        if (player.teamData.invisibleNameTagView != visible) return;
        player.teamData.invisibleNameTagView = !visible;
        if (sendToggleMessage) {
            MessageFile messageFile = TAB.getInstance().getConfiguration().getMessages();
            player.sendMessage(visible ? messageFile.getNameTagViewShown() :messageFile.getNameTagViewHidden());
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(player, visible);
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            updateVisibility(all, player);
        }
    }

    @Override
    public boolean hasHiddenNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).teamData.invisibleNameTagView;
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "NameTags";
    }
}