package me.neznamy.tab.shared.features.nametags;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class NameTag extends RefreshableFeature implements NameTagManager, JoinListener, QuitListener,
        Loadable, WorldSwitchListener, ServerSwitchListener, VanishListener, CustomThreaded, ProxyFeature, GroupListener {

    private final ThreadExecutor customThread = new ThreadExecutor("TAB NameTag Thread");
    private OnlinePlayers onlinePlayers;
    private final TeamConfiguration configuration;
    private final StringToComponentCache cache = new StringToComponentCache("NameTags", 1000);
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
        disableChecker = new DisableChecker(this, Condition.getCondition(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.teamData.disabled);
        collisionManager = new CollisionManager(this);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, new VisibilityRefresher(this));
        if (proxy != null) {
            proxy.registerMessage("teams", UpdateProxyPlayer.class, UpdateProxyPlayer::new);
        }
    }

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        collisionManager.load();
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            ((SafeScoreboard<?>)all.getScoreboard()).setAntiOverrideTeams(configuration.isAntiOverride());
            loadProperties(all);
            all.teamData.teamName = all.sortingData.shortTeamName; // Sorting is loaded sync before nametags
            if (disableChecker.isDisableConditionMet(all)) {
                all.teamData.disabled.set(true);
                continue;
            }
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(all, true);
            if (proxy != null) {
                proxy.sendMessage(new UpdateProxyPlayer(
                        all.getTablistId(),
                        all.teamData.teamName,
                        all.teamData.prefix.get(),
                        all.teamData.suffix.get(),
                        getTeamVisibility(all, all) ? NameVisibility.ALWAYS : NameVisibility.NEVER
                ));
            }
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
        ((SafeScoreboard<?>)connectedPlayer.getScoreboard()).setAntiOverrideTeams(configuration.isAntiOverride());
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
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.teamData.disabled.set(true);
            return;
        }
        registerTeam(connectedPlayer);
        if (proxy != null) {
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.getTagPrefix() == null) continue; // This proxy player is not loaded yet
                TabComponent prefix = cache.get(proxied.getTagPrefix());
                connectedPlayer.getScoreboard().registerTeam(
                        proxied.getTeamName(),
                        prefix,
                        cache.get(proxied.getTagSuffix()),
                        proxied.getNameVisibility(),
                        CollisionRule.ALWAYS,
                        Collections.singletonList(proxied.getNickname()),
                        2,
                        prefix.getLastColor()
                );
            }
            proxy.sendMessage(new UpdateProxyPlayer(
                    connectedPlayer.getTablistId(),
                    connectedPlayer.teamData.teamName,
                    connectedPlayer.teamData.prefix.get(),
                    connectedPlayer.teamData.suffix.get(),
                    getTeamVisibility(connectedPlayer, connectedPlayer) ? NameVisibility.ALWAYS : NameVisibility.NEVER
            ));
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
    public void onServerChange(@NonNull TabPlayer p, @NonNull String from, @NonNull String to) {
        if (updateProperties(p) && !p.teamData.isDisabled()) updatePrefixSuffix(p);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
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
            TabComponent prefix = cache.get(player.teamData.prefix.getFormat(viewer));
            viewer.getScoreboard().updateTeam(
                    player.teamData.teamName,
                    prefix,
                    cache.get(player.teamData.suffix.getFormat(viewer)),
                    prefix.getLastColor()
            );
        }
        if (proxy != null) proxy.sendMessage(new UpdateProxyPlayer(
                player.getTablistId(),
                player.teamData.teamName,
                player.teamData.prefix.get(),
                player.teamData.suffix.get(),
                getTeamVisibility(player, player) ? NameVisibility.ALWAYS : NameVisibility.NEVER
        ));
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
            if (proxy != null) proxy.sendMessage(new UpdateProxyPlayer(player.getTablistId(), player.teamData.teamName,
                    player.teamData.prefix.get(),
                    player.teamData.suffix.get(),
                    getTeamVisibility(player, player) ? NameVisibility.ALWAYS : NameVisibility.NEVER
            ));
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
        TabComponent prefix = cache.get(p.teamData.prefix.getFormat(viewer));
        viewer.getScoreboard().registerTeam(
                p.teamData.teamName,
                prefix,
                cache.get(p.teamData.suffix.getFormat(viewer)),
                getTeamVisibility(p, viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                p.teamData.getCollisionRule() ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                Collections.singletonList(p.getNickname()),
                teamOptions,
                prefix.getLastColor()
        );
    }

    public boolean getTeamVisibility(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (viewer.getVersion().getMinorVersion() == 8 && p.hasInvisibilityPotion()) return false;
        return !hasHiddenNameTag(p) && !hasHiddenNameTag(p, viewer) && !configuration.isInvisibleNameTags() && !viewer.teamData.invisibleNameTagView;
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
            if (proxy != null) proxy.sendMessage(new UpdateProxyPlayer(
                    player.getTablistId(),
                    player.teamData.teamName,
                    player.teamData.prefix.get(),
                    player.teamData.suffix.get(),
                    getTeamVisibility(player, player) ? NameVisibility.ALWAYS : NameVisibility.NEVER
            ));
        }, getFeatureName(), "Updating team name"));
    }

    // ------------------
    // ProxySupport
    // ------------------

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            proxy.sendMessage(new UpdateProxyPlayer(
                    all.getTablistId(),
                    all.teamData.teamName,
                    all.teamData.prefix.get(),
                    all.teamData.suffix.get(),
                    getTeamVisibility(all, all) ? NameVisibility.ALWAYS : NameVisibility.NEVER
            ));
        }
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        if (player.getTeamName() == null) {
            TAB.getInstance().getErrorManager().printError("Unable to unregister team of proxy player " + player.getName() + " on quit, because team is null", null);
            return;
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (viewer.getUniqueId().equals(player.getUniqueId())) continue;
            ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(player.getTeamName());
        }
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (!p.teamData.hiddenNameTag) {
                p.teamData.hiddenNameTag = true;
                updateVisibility(p);
            }
        }, getFeatureName(), "Processing API call (hideNameTag)"));

    }

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (!p.teamData.addHiddenNameTagFor((TabPlayer) viewer)) return;
            updateVisibility(p, (TabPlayer) viewer);
        }, getFeatureName(), "Processing API call (hideNameTag)"));
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (p.teamData.hiddenNameTag) {
                p.teamData.hiddenNameTag = false;
                updateVisibility(p);
            }
        }, getFeatureName(), "Processing API call (showNameTag)"));
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (!p.teamData.removeHiddenNameTagFor((TabPlayer) viewer)) return;
            updateVisibility(p, (TabPlayer) viewer);
        }, getFeatureName(), "Processing API call (showNameTag)"));
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).teamData.hiddenNameTag;
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        ensureActive();
        return ((TabPlayer)player).teamData.hasHiddenNameTagFor((TabPlayer) viewer);
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
    public String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getOriginalRawValue();
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        ensureActive();
        TabPlayer player = (TabPlayer) p;
        if (player.teamData.invisibleNameTagView) {
            player.teamData.invisibleNameTagView = false;
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsShown());
        } else {
            player.teamData.invisibleNameTagView = true;
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsHidden());
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(player, !player.teamData.invisibleNameTagView);
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

    /**
     * Class holding team data for players.
     */
    public static class PlayerData {

        /** Team name used for sorting */
        public String teamName;

        /** Player's tagprefix */
        public Property prefix;

        /** Player's tagsuffix */
        public Property suffix;

        /** Flag tracking whether this feature is disabled for the player with condition or not */
        public final AtomicBoolean disabled = new AtomicBoolean();
        
        /** Flag tracking whether this player has name tag hidden globally using API or not */
        public boolean hiddenNameTag;

        /** Players who should not see this player's name tag */
        @Nullable
        private Set<TabPlayer> hiddenNameTagFor;

        /** Flag tracking whether team handling is paused or not */
        public boolean teamHandlingPaused;

        /** Flag tracking whether this player disabled nametags on all players or not */
        public boolean invisibleNameTagView;

        /** Players who this player is vanished for */
        public final Set<UUID> vanishedFor = new HashSet<>();
        
        /** Currently used collision rule */
        public boolean collisionRule;

        /** Forced collision rule using API */
        @Nullable
        public Boolean forcedCollision;

        /**
         * Returns current collision rule. If forced using API, the forced value is returned.
         * Otherwise, value assigned internally based on configuration is returned.
         *
         * @return  Current collision rule to use
         */
        public boolean getCollisionRule() {
            return forcedCollision != null ? forcedCollision : collisionRule;
        }

        /**
         * Returns {@code true} if nametag is hidden for specified viewer, {@code false} if not.
         *
         * @param   viewer
         *          Player viewing the nametag
         * @return  {@code true} if hidden for viewer, {@code false} if not
         */
        public boolean hasHiddenNameTagFor(@NotNull TabPlayer viewer) {
            if (hiddenNameTagFor == null) return false;
            return hiddenNameTagFor.contains(viewer);
        }

        /**
         * Adds player to players to hide nametag for.
         *
         * @param   viewer
         *          Player to hide nametag for
         * @return  {@code true} if player was added, {@code false} if player was already added before
         */
        public boolean addHiddenNameTagFor(@NotNull TabPlayer viewer) {
            if (hiddenNameTagFor == null) hiddenNameTagFor = Collections.newSetFromMap(new WeakHashMap<>());
            return hiddenNameTagFor.add(viewer);
        }

        /**
         * Removes player from players to hide nametag for.
         *
         * @param   viewer
         *          Player to show back nametag for
         * @return  {@code true} if player was remove, {@code false} if player was not in list
         */
        public boolean removeHiddenNameTagFor(@NotNull TabPlayer viewer) {
            if (hiddenNameTagFor != null) return hiddenNameTagFor.remove(viewer);
            return false;
        }

        /**
         * Returns {@code true} if teams are disabled for this player either with condition
         * or with the API, {@code false} otherwise.
         *
         * @return  {@code true} if teams are disabled for the player, {@code false} if not
         */
        public boolean isDisabled() {
            return disabled.get() || teamHandlingPaused;
        }
    }

    /**
     * Proxy message to update team data of a player.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    private class UpdateProxyPlayer extends ProxyMessage {

        private UUID playerId;
        private String teamName;
        private String prefix;
        private String suffix;
        private NameVisibility nameVisibility;

        @NotNull
        public ThreadExecutor getCustomThread() {
            return customThread;
        }

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(teamName);
            out.writeUTF(prefix);
            out.writeUTF(suffix);
            out.writeUTF(nameVisibility.toString());
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            teamName = in.readUTF();
            prefix = in.readUTF();
            suffix = in.readUTF();
            nameVisibility = NameVisibility.getByName(in.readUTF());
        }

        @Override
        public void process(@NotNull ProxySupport proxySupport) {
            ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
            if (target == null) {
                TAB.getInstance().getErrorManager().printError("Unable to process nametag update of proxy player " + playerId + ", because no such player exists", null);
                return;
            }
            if (target.getTeamName() == null) {
                TAB.getInstance().debug("Processing nametag join of proxy player " + target.getName());
            }
            String oldTeamName = target.getTeamName();
            String newTeamName = checkTeamName(target, teamName.substring(0, teamName.length()-1));
            target.setTeamName(newTeamName);
            target.setTagPrefix(prefix);
            target.setTagSuffix(suffix);
            target.setNameVisibility(nameVisibility);
            TabComponent prefixComponent = cache.get(prefix);
            if (!newTeamName.equals(oldTeamName)) {
                for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                    if (viewer.getUniqueId().equals(target.getUniqueId())) continue;
                    if (oldTeamName != null) viewer.getScoreboard().unregisterTeam(oldTeamName);
                    viewer.getScoreboard().registerTeam(
                            newTeamName,
                            prefixComponent,
                            cache.get(suffix),
                            nameVisibility,
                            CollisionRule.ALWAYS,
                            Collections.singletonList(target.getNickname()),
                            2,
                            prefixComponent.getLastColor()
                    );
                }
            } else {
                for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                    viewer.getScoreboard().updateTeam(
                            oldTeamName,
                            prefixComponent,
                            cache.get(suffix),
                            nameVisibility,
                            CollisionRule.ALWAYS,
                            2,
                            prefixComponent.getLastColor()
                    );
                }
            }
        }

        @NotNull
        private String checkTeamName(@NotNull ProxyPlayer player, @NotNull String currentName15) {
            char id = 'A';
            while (true) {
                String potentialTeamName = currentName15 + id;
                boolean nameTaken = false;
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (potentialTeamName.equals(all.sortingData.shortTeamName)) {
                        nameTaken = true;
                        break;
                    }
                }
                if (!nameTaken && proxy != null) {
                    for (ProxyPlayer all : proxy.getProxyPlayers().values()) {
                        if (all == player) continue;
                        if (potentialTeamName.equals(all.getTeamName())) {
                            nameTaken = true;
                            break;
                        }
                    }
                }
                if (!nameTaken) {
                    return potentialTeamName;
                }
                id++;
            }
        }
    }
}