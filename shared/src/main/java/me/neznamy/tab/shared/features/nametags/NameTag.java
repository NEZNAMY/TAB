package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.util.Preconditions;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NameTag extends TabFeature implements NameTagManager, JoinListener, QuitListener,
        Loadable, UnLoadable, WorldSwitchListener, ServerSwitchListener, Refreshable, LoginPacketListener,
        VanishListener {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating prefix/suffix";
    protected final boolean invisibleNameTags = config().getBoolean("scoreboard-teams.invisible-nametags", false);
    private final boolean canSeeFriendlyInvisibles = config().getBoolean("scoreboard-teams.can-see-friendly-invisibles", false);
    @Getter private final Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
    @Getter private final CollisionManager collisionManager = new CollisionManager(this);
    @Getter private final int teamOptions = canSeeFriendlyInvisibles ? 2 : 0;

    private final Set<me.neznamy.tab.api.TabPlayer> hiddenNameTag = Collections.newSetFromMap(new WeakHashMap<>());
    protected final Set<me.neznamy.tab.api.TabPlayer> teamHandlingPaused = Collections.newSetFromMap(new WeakHashMap<>());
    protected final WeakHashMap<me.neznamy.tab.api.TabPlayer, Set<me.neznamy.tab.api.TabPlayer>> hiddenNameTagFor = new WeakHashMap<>();
    protected final Set<me.neznamy.tab.api.TabPlayer> playersWithInvisibleNameTagView = Collections.newSetFromMap(new WeakHashMap<>());
    @Getter private final DisableChecker disableChecker;
    private RedisSupport redis;

    // Key - Vanished player, Value = List of players who cannot see the player (UUIDs to prevent memory leak)
    private final WeakHashMap<TabPlayer, List<UUID>> vanishedPlayers = new WeakHashMap<>();

    public NameTag() {
        Condition disableCondition = Condition.getCondition(config().getString("scoreboard-teams.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        if (!config().getBoolean("scoreboard-teams.anti-override", true))
            TAB.getInstance().getConfigHelper().startup().teamAntiOverrideDisabled();
    }

    @Override
    public void load() {
        // RedisSupport is instantiated after NameTags, so must be loaded after
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, new VisibilityRefresher(this));
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateProperties(all);
            hiddenNameTagFor.put(all, new HashSet<>());
            if (disableChecker.isDisableConditionMet(all)) {
                disableChecker.addDisabledPlayer(all);
                continue;
            }
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(all, true);
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (target.isVanished() && !TAB.getInstance().getPlatform().canSee(viewer, target)) {
                    vanishedPlayers.computeIfAbsent(target, p -> new ArrayList<>()).add(viewer.getUniqueId());
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
                viewer.getScoreboard().unregisterTeam(sorting.getShortTeamName(target));
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
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        updateProperties(connectedPlayer);
        hiddenNameTagFor.put(connectedPlayer, new HashSet<>());
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == connectedPlayer) continue; //avoiding double registration
            if (connectedPlayer.isVanished() && !TAB.getInstance().getPlatform().canSee(all, connectedPlayer)) {
                vanishedPlayers.computeIfAbsent(connectedPlayer, p -> new ArrayList<>()).add(all.getUniqueId());
            }
            if (all.isVanished() && !TAB.getInstance().getPlatform().canSee(connectedPlayer, all)) {
                vanishedPlayers.computeIfAbsent(all, p -> new ArrayList<>()).add(connectedPlayer.getUniqueId());
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
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == disconnectedPlayer) continue; //player who just disconnected
                if (!TAB.getInstance().getPlatform().canSee(viewer, disconnectedPlayer)) continue;
                viewer.getScoreboard().unregisterTeam(sorting.getShortTeamName(disconnectedPlayer));
            }
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == disconnectedPlayer) continue;
            hiddenNameTagFor.getOrDefault(all, Collections.emptySet()).remove(disconnectedPlayer); //clearing memory from API method
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
            unregisterTeam(p, sorting.getShortTeamName(p));
        } else {
            registerTeam(p);
        }
    }

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        if (!hiddenNameTag.add(player)) return;
        updateTeamData((TabPlayer) player);
    }
    
    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        if (!hiddenNameTagFor.get(player).add(viewer)) return;
        updateTeamData((TabPlayer) player, (TabPlayer) viewer);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        if (!hiddenNameTag.remove(player)) return;
        updateTeamData((TabPlayer) player);
    }
    
    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        if (!hiddenNameTagFor.get(player).remove(viewer)) return;
        updateTeamData((TabPlayer) player, (TabPlayer) viewer);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return hiddenNameTag.contains(player);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        return hiddenNameTagFor.containsKey(player) && hiddenNameTagFor.get(player).contains(viewer);
    }

    @Override
    public void pauseTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        if (teamHandlingPaused.contains(player)) return;
        if (!disableChecker.isDisabledPlayer((TabPlayer) player)) unregisterTeam((TabPlayer) player, sorting.getShortTeamName((TabPlayer) player));
        teamHandlingPaused.add(player); //adding after, so unregisterTeam method runs
    }

    @Override
    public void resumeTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        if (!teamHandlingPaused.remove(player)) return; //removing before, so registerTeam method runs
        if (!disableChecker.isDisabledPlayer((TabPlayer) player)) registerTeam((TabPlayer) player);
    }

    @Override
    public boolean hasTeamHandlingPaused(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return teamHandlingPaused.contains(player);
    }

    @Override
    public void setCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player, Boolean collision) {
        collisionManager.setCollisionRule((TabPlayer) player, collision);
    }

    @Override
    public Boolean getCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return collisionManager.getCollisionRule((TabPlayer) player);
    }
    
    public void updateTeamData(@NonNull TabPlayer p) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(p, viewer);
        }
        if (redis != null) redis.updateTeam(p, sorting.getShortTeamName(p),
                p.getProperty(TabConstants.Property.TAGPREFIX).get(),
                p.getProperty(TabConstants.Property.TAGSUFFIX).get(),
                getTeamVisibility(p, p) ? NameVisibility.ALWAYS : NameVisibility.NEVER);
    }

    public void updateTeamData(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (!TAB.getInstance().getPlatform().canSee(viewer, p) && p != viewer) return;
        boolean visible = getTeamVisibility(p, viewer);
        String prefix = p.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
        viewer.getScoreboard().updateTeam(
                sorting.getShortTeamName(p),
                prefix,
                p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer),
                visible ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                collisionManager.getCollision(p) ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                teamOptions,
                EnumChatFormat.lastColorsOf(prefix)
        );
    }

    public void unregisterTeam(@NonNull TabPlayer p, @NonNull String teamName) {
        if (hasTeamHandlingPaused(p)) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (!TAB.getInstance().getPlatform().canSee(viewer, p) && p != viewer) continue;
            viewer.getScoreboard().unregisterTeam(teamName);
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
                sorting.getShortTeamName(p),
                prefix,
                p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer),
                getTeamVisibility(p, viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                collisionManager.getCollision(p) ? CollisionRule.ALWAYS : CollisionRule.NEVER,
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
                && !p.hasInvisibilityPotion() && !playersWithInvisibleNameTagView.contains(viewer);
    }

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        Preconditions.checkLoaded(player);
        ((TabPlayer)player).getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(prefix);
        updateTeamData((TabPlayer) player);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        Preconditions.checkLoaded(player);
        ((TabPlayer)player).getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(suffix);
        updateTeamData((TabPlayer) player);
    }

    @Override
    public String getCustomPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TAGPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TAGSUFFIX).getTemporaryValue();
    }

    @Override
    public @NonNull String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TAGPREFIX).getOriginalRawValue();
    }

    @Override
    public @NonNull String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.TAGSUFFIX).getOriginalRawValue();
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        TabPlayer player = (TabPlayer) p;
        if (playersWithInvisibleNameTagView.contains(player)) {
            playersWithInvisibleNameTagView.remove(player);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsShown(), true);
        } else {
            playersWithInvisibleNameTagView.add(player);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsHidden(), true);
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(player, !playersWithInvisibleNameTagView.contains(player));
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(all, player);
        }
    }

    @Override
    public boolean hasHiddenNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return playersWithInvisibleNameTagView.contains(player);
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
                    vanishedPlayers.computeIfAbsent(player, p -> new ArrayList<>()).add(viewer.getUniqueId());
                    viewer.getScoreboard().unregisterTeam(sorting.getShortTeamName(player));
                }
            }
        } else {
            for (UUID id : vanishedPlayers.getOrDefault(player, Collections.emptyList())) {
                TabPlayer viewer = TAB.getInstance().getPlayer(id);
                if (viewer != null) registerTeam(player, viewer);
            }
            vanishedPlayers.remove(player);
        }
    }
}