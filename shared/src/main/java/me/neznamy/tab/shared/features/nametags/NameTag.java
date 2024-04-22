package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NameTag extends TabFeature implements NameTagManager, JoinListener, QuitListener,
        Loadable, UnLoadable, WorldSwitchListener, ServerSwitchListener, Refreshable, LoginPacketListener,
        VanishListener {

    protected final boolean invisibleNameTags = config().getBoolean("scoreboard-teams.invisible-nametags", false);
    private final boolean canSeeFriendlyInvisibles = config().getBoolean("scoreboard-teams.can-see-friendly-invisibles", false);
    private final boolean antiOverride = config().getBoolean("scoreboard-teams.anti-override", true);

    @Getter private final CollisionManager collisionManager = new CollisionManager(this);
    @Getter private final int teamOptions = canSeeFriendlyInvisibles ? 2 : 0;
    @Getter private final DisableChecker disableChecker;
    private RedisSupport redis;

    public NameTag() {
        Condition disableCondition = Condition.getCondition(config().getString("scoreboard-teams.disable-condition"));
        disableChecker = new DisableChecker(getFeatureName(), disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS + "-Condition", disableChecker);
        if (!antiOverride) TAB.getInstance().getConfigHelper().startup().teamAntiOverrideDisabled();
    }

    @Override
    public void load() {
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        collisionManager.load();
        // RedisSupport is instantiated after NameTags, so must be loaded after
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, new VisibilityRefresher(this));
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            all.teamData = new PlayerData();
            all.getScoreboard().setAntiOverrideTeams(antiOverride);
            updateProperties(all);
            if (disableChecker.isDisableConditionMet(all)) {
                disableChecker.addDisabledPlayer(all);
                continue;
            }
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(all, true);
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (target.isVanished() && !TAB.getInstance().getPlatform().canSee(viewer, target)) {
                    target.teamData.vanishedFor.add(viewer.getUniqueId());
                }
                if (!disableChecker.isDisabledPlayer(target)) registerTeam(target, viewer);
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (hasTeamHandlingPaused(target) || disableChecker.isDisabledPlayer(target)) continue;
                viewer.getScoreboard().unregisterTeam(target.sortingData.getShortTeamName());
            }
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (disableChecker.isDisabledPlayer(refreshed)) return;
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.getProperty(TabConstants.Property.TAGPREFIX).update();
            boolean suffix = refreshed.getProperty(TabConstants.Property.TAGSUFFIX).update();
            refresh = prefix || suffix;
        }
        if (refresh) updateTeamData(refreshed);
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating prefix/suffix";
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.teamData = new PlayerData();
        connectedPlayer.getScoreboard().setAntiOverrideTeams(antiOverride);
        updateProperties(connectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == connectedPlayer) continue; //avoiding double registration
            if (connectedPlayer.isVanished() && !TAB.getInstance().getPlatform().canSee(all, connectedPlayer)) {
                connectedPlayer.teamData.vanishedFor.add(all.getUniqueId());
            }
            if (all.isVanished() && !TAB.getInstance().getPlatform().canSee(connectedPlayer, all)) {
                all.teamData.vanishedFor.add(connectedPlayer.getUniqueId());
            }
            if (!disableChecker.isDisabledPlayer(all)) {
                registerTeam(all, connectedPlayer);
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(connectedPlayer, true);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
            return;
        }
        registerTeam(connectedPlayer);
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        if (!disableChecker.isDisabledPlayer(disconnectedPlayer) && !hasTeamHandlingPaused(disconnectedPlayer)) {
            String teamName = disconnectedPlayer.sortingData.getShortTeamName();
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == disconnectedPlayer) continue; //player who just disconnected
                if (viewer.getScoreboard().containsTeam(teamName)) {
                    viewer.getScoreboard().unregisterTeam(teamName);
                }
            }
        }
    }

    @Override
    public void onServerChange(@NonNull TabPlayer p, @NonNull String from, @NonNull String to) {
        if (updateProperties(p) && !disableChecker.isDisabledPlayer(p)) updateTeamData(p);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        if (updateProperties(changed) && !disableChecker.isDisabledPlayer(changed)) updateTeamData(changed);
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            unregisterTeam(p, p.sortingData.getShortTeamName());
        } else {
            registerTeam(p);
        }
    }
    
    public void updateTeamData(@NonNull TabPlayer p) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(p, viewer);
        }
        if (redis != null) redis.updateTeam(p, p.sortingData.getShortTeamName(),
                p.getProperty(TabConstants.Property.TAGPREFIX).get(),
                p.getProperty(TabConstants.Property.TAGSUFFIX).get(),
                getTeamVisibility(p, p) ? NameVisibility.ALWAYS : NameVisibility.NEVER);
    }

    public void updateTeamData(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (!viewer.getScoreboard().containsTeam(p.sortingData.getShortTeamName())) return;
        boolean visible = getTeamVisibility(p, viewer);
        String prefix = p.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
        viewer.getScoreboard().updateTeam(
                p.sortingData.getShortTeamName(),
                prefix,
                p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer),
                visible ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                p.teamData.getCollisionRule() ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                teamOptions,
                EnumChatFormat.lastColorsOf(prefix)
        );
    }

    public void unregisterTeam(@NonNull TabPlayer p, @NonNull String teamName) {
        if (hasTeamHandlingPaused(p)) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getScoreboard().containsTeam(teamName)) {
                viewer.getScoreboard().unregisterTeam(teamName);
            }
        }
    }

    public void registerTeam(@NonNull TabPlayer p) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            registerTeam(p, viewer);
        }
    }

    private void registerTeam(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (hasTeamHandlingPaused(p)) return;
        if (!TAB.getInstance().getPlatform().canSee(viewer, p) && p != viewer) return;
        String prefix = p.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
        viewer.getScoreboard().registerTeam(
                p.sortingData.getShortTeamName(),
                prefix,
                p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer),
                getTeamVisibility(p, viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                p.teamData.getCollisionRule() ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                Collections.singletonList(p.getNickname()),
                teamOptions,
                EnumChatFormat.lastColorsOf(prefix)
        );
    }

    protected boolean updateProperties(@NonNull TabPlayer p) {
        boolean changed = p.loadPropertyFromConfig(this, TabConstants.Property.TAGPREFIX);
        if (p.loadPropertyFromConfig(this, TabConstants.Property.TAGSUFFIX)) changed = true;
        return changed;
    }

    public boolean getTeamVisibility(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        return !hasHiddenNameTag(p) && !hasHiddenNameTag(p, viewer) && !invisibleNameTags
                && !p.hasInvisibilityPotion() && !viewer.teamData.invisibleNameTagView;
    }

    @Override
    public void onLoginPacket(TabPlayer player) {
        if (!player.isLoaded()) return;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!disableChecker.isDisabledPlayer(all) && all.isLoaded()) registerTeam(all, player);
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished()) {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == player) continue;
                if (!TAB.getInstance().getPlatform().canSee(viewer, player)) {
                    player.teamData.vanishedFor.add(viewer.getUniqueId());
                    viewer.getScoreboard().unregisterTeam(player.sortingData.getShortTeamName());
                }
            }
        } else {
            for (UUID id : player.teamData.vanishedFor) {
                TabPlayer viewer = TAB.getInstance().getPlayer(id);
                if (viewer != null) registerTeam(player, viewer);
            }
            player.teamData.vanishedFor.clear();
        }
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "NameTags";
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.hiddenNameTag) {
            p.teamData.hiddenNameTag = true;
            updateTeamData(p);
        }
    }

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.hiddenNameTagFor.add((TabPlayer) viewer)) return;
        updateTeamData(p, (TabPlayer) viewer);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (p.teamData.hiddenNameTag) {
            p.teamData.hiddenNameTag = false;
            updateTeamData(p);
        }
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.hiddenNameTagFor.remove((TabPlayer) viewer)) return;
        updateTeamData(p, (TabPlayer) viewer);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return ((TabPlayer)player).teamData.hiddenNameTag;
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        return ((TabPlayer)player).teamData.hiddenNameTagFor.contains((TabPlayer) viewer);
    }

    @Override
    public void pauseTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (p.teamData.teamHandlingPaused) return;
        if (!disableChecker.isDisabledPlayer(p)) unregisterTeam(p, p.sortingData.getShortTeamName());
        p.teamData.teamHandlingPaused = true; //setting after, so unregisterTeam method runs
    }

    @Override
    public void resumeTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.teamHandlingPaused) return;
        p.teamData.teamHandlingPaused = false; //setting before, so registerTeam method runs
        if (!disableChecker.isDisabledPlayer(p)) registerTeam(p);
    }

    @Override
    public boolean hasTeamHandlingPaused(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return ((TabPlayer)player).teamData.teamHandlingPaused;
    }

    @Override
    public void setCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player, Boolean collision) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (Objects.equals(p.teamData.forcedCollision, collision)) return;
        p.teamData.forcedCollision = collision;
        updateTeamData(p);
    }

    @Override
    public Boolean getCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.forcedCollision;
    }

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        p.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(prefix);
        updateTeamData(p);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        p.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(suffix);
        updateTeamData(p);
    }

    @Override
    public String getCustomPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(TabConstants.Property.TAGPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(TabConstants.Property.TAGSUFFIX).getTemporaryValue();
    }

    @Override
    @NonNull
    public String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(TabConstants.Property.TAGPREFIX).getOriginalRawValue();
    }

    @Override
    @NonNull
    public String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(TabConstants.Property.TAGSUFFIX).getOriginalRawValue();
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        TabPlayer player = (TabPlayer) p;
        if (player.teamData.invisibleNameTagView) {
            player.teamData.invisibleNameTagView = false;
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsShown(), true);
        } else {
            player.teamData.invisibleNameTagView = true;
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsHidden(), true);
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(player, !player.teamData.invisibleNameTagView);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(all, player);
        }
    }

    @Override
    public boolean hasHiddenNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return ((TabPlayer)player).teamData.invisibleNameTagView;
    }

    /**
     * Class holding team data for players.
     */
    public static class PlayerData {

        /** Flag tracking whether this player has name tag hidden globally using API or not */
        public boolean hiddenNameTag;

        /** Players who should not see this player's name tag */
        public Set<TabPlayer> hiddenNameTagFor = Collections.newSetFromMap(new WeakHashMap<>());

        /** Flag tracking whether team handling is paused or not */
        public boolean teamHandlingPaused;

        /** Flag tracking whether this player disabled nametags on all players or not */
        public boolean invisibleNameTagView;

        /** Players who this player is vanished for */
        public Set<UUID> vanishedFor = new HashSet<>();
        
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
    }
}