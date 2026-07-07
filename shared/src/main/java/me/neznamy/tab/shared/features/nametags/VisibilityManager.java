package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Sub-feature for NameTag feature that manages nametag visibility.
 */
public class VisibilityManager extends RefreshableFeature implements JoinListener, Loadable, CustomThreaded {

    /** The main feature */
    @NotNull private final NameTag nameTags;

    /** Configured condition for invisible nametags */
    @Getter
    @NotNull
    private final Condition invisibleCondition;

    /** Viewers using opaque nametag view for all players */
    @NotNull
    private final Set<TabPlayer> opaqueNameTagViewers = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Constructs new instance.
     *
     * @param   nameTags
     *          Parent feature
     */
    public VisibilityManager(@NotNull NameTag nameTags) {
        this.nameTags = nameTags;
        invisibleCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(nameTags.getConfiguration().getInvisibleNameTags());
    }

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, p -> {
            TabPlayer player = (TabPlayer) p;
            boolean newInvisibility = invisibleCondition.isMet((TabPlayer) p);
            if (newInvisibility) {
                player.teamData.hideNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
            } else {
                player.teamData.showNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
            }
            if (player.hasInvisibilityPotion()) {
                newInvisibility = true;
            }
            return Boolean.toString(newInvisibility);
        });
        addUsedPlaceholder(TabConstants.Placeholder.INVISIBLE);
        for (TabPlayer all : nameTags.getOnlinePlayers().getPlayers()) {
            onJoin(all);
        }
        getCustomThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(),
                this::requestOpaqueOcclusionRefresh, getFeatureName(), "Updating opaque nametag occlusion"), 250);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        if (invisibleCondition.isMet(connectedPlayer)) {
            connectedPlayer.teamData.hideNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
            updateVisibility(connectedPlayer);
        }
        if (!opaqueNameTagViewers.isEmpty()) requestOpaqueOcclusionRefresh();
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating NameTag visibility";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.teamData.isDisabled()) return;
        if (!nameTags.getOnlinePlayers().contains(p)) return; // player is not loaded by this feature yet
        updateVisibility(p);
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return nameTags.getCustomThread();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return nameTags.getFeatureName();
    }

    /**
     * Updates visibility of a player for everyone.
     *
     * @param   player
     *          Player to update visibility of
     */
    public void updateVisibility(@NonNull TabPlayer player) {
        for (TabPlayer viewer : nameTags.getOnlinePlayers().getPlayers()) {
            if (viewer.teamData.hasTeamRegistered(player)) {
                viewer.getScoreboard().updateTeam(
                        player.teamData.teamName,
                        player.teamData.getTeamVisibility(viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER
                );
            }
        }
        nameTags.getProxyHandler().sendProxyMessage(player);
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
        if (viewer.teamData.hasTeamRegistered(player)) {
            viewer.getScoreboard().updateTeam(
                    player.teamData.teamName,
                    player.teamData.getTeamVisibility(viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER
            );
        }
    }

    public void hideNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason,
                            boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            clearOpaqueNameTagMode(player, null);
            if (player.teamData.hideNametag(reason)) {
                updateVisibility(player);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
        }, getFeatureName(), cpuReason));
    }

    public void hideNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason,
                            @NonNull String cpuReason, boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            clearOpaqueNameTagMode(player, viewer);
            if (player.teamData.hideNametag(viewer, reason)) {
                updateVisibility(player, viewer);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
        }, getFeatureName(), cpuReason));
    }

    public void showNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason,
                            boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            clearOpaqueNameTagMode(player, null);
            if (player.teamData.showNametag(reason)) {
                updateVisibility(player);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
        }, getFeatureName(), cpuReason));
    }

    public void showNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason,
                            @NonNull String cpuReason, boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            clearOpaqueNameTagMode(player, viewer);
            if (player.teamData.showNametag(viewer, reason)) {
                updateVisibility(player, viewer);
            }
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
        }, getFeatureName(), cpuReason));
    }

    public void toggleNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason,
                              boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            clearOpaqueNameTagMode(player, null);
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
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            clearOpaqueNameTagMode(player, viewer);
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

    public void setOpaqueNameTag(@NonNull TabPlayer player, @Nullable TabPlayer viewer, @NonNull String cpuReason,
                                 boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            player.teamData.setOpaqueNameTagMode(viewer, true);
            if (viewer != null) {
                player.teamData.showNametag(viewer, NameTagInvisibilityReason.HIDE_COMMAND);
                player.teamData.showNametag(viewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION);
                updateVisibility(player, viewer);
            } else {
                player.teamData.showNametag(NameTagInvisibilityReason.HIDE_COMMAND);
                for (TabPlayer currentViewer : nameTags.getOnlinePlayers().getPlayers()) {
                    player.teamData.showNametag(currentViewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION);
                }
                updateVisibility(player);
            }
            requestOpaqueOcclusionRefresh();
            if (sendMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
        }, getFeatureName(), cpuReason));
    }

    public void setOpaqueNameTagView(@NonNull TabPlayer viewer, @NonNull String cpuReason, boolean sendMessage) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            viewer.teamData.invisibleNameTagView = false;
            viewer.expansionData.setNameTagVisibility(true);
            opaqueNameTagViewers.add(viewer);
            for (TabPlayer player : nameTags.getOnlinePlayers().getPlayers()) {
                player.teamData.showNametag(viewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION);
                updateVisibility(player, viewer);
            }
            requestOpaqueOcclusionRefresh();
            if (sendMessage) viewer.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagViewShown());
        }, getFeatureName(), cpuReason));
    }

    public void clearOpaqueNameTagView(@NonNull TabPlayer viewer, @NonNull String cpuReason) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            opaqueNameTagViewers.remove(viewer);
            for (TabPlayer player : nameTags.getOnlinePlayers().getPlayers()) {
                if (!isOpaqueNameTagMode(player, viewer) && player.teamData.showNametag(viewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION)) {
                    updateVisibility(player, viewer);
                }
            }
        }, getFeatureName(), cpuReason));
    }

    private void requestOpaqueOcclusionRefresh() {
        List<OpaqueVisibilityCheck> checks = new ArrayList<>();
        for (TabPlayer player : nameTags.getOnlinePlayers().getPlayers()) {
            if (!hasOpaqueNameTagMode(player) || player.teamData.isDisabled()) continue;
            for (TabPlayer viewer : nameTags.getOnlinePlayers().getPlayers()) {
                checks.add(new OpaqueVisibilityCheck(player, viewer, !isOpaqueNameTagMode(player, viewer)));
            }
        }
        if (checks.isEmpty()) return;
        TAB.getInstance().getPlatform().runSyncGlobal(() -> {
            List<OpaqueVisibilityResult> results = new ArrayList<>(checks.size());
            for (OpaqueVisibilityCheck check : checks) {
                if (!check.player.isOnline() || !check.viewer.isOnline()) {
                    results.add(new OpaqueVisibilityResult(check.player, check.viewer, true));
                    continue;
                }
                results.add(new OpaqueVisibilityResult(check.player, check.viewer,
                        check.forceVisible || check.viewer == check.player || hasLineOfSight(check.viewer, check.player)));
            }
            getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> applyOpaqueOcclusionResults(results), getFeatureName(), "Applying opaque nametag occlusion"));
        });
    }

    private void applyOpaqueOcclusionResults(@NonNull List<OpaqueVisibilityResult> results) {
        for (OpaqueVisibilityResult result : results) {
            if (!nameTags.getOnlinePlayers().contains(result.player) || !nameTags.getOnlinePlayers().contains(result.viewer)) continue;
            boolean visible = result.visible;
            if (!hasOpaqueNameTagMode(result.player) || result.player.teamData.isDisabled()) {
                visible = true;
            } else if (!isOpaqueNameTagMode(result.player, result.viewer)) {
                visible = true;
            }
            boolean changed = visible
                    ? result.player.teamData.showNametag(result.viewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION)
                    : result.player.teamData.hideNametag(result.viewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION);
            if (changed) updateVisibility(result.player, result.viewer);
        }
    }

    private void clearOpaqueNameTagMode(@NonNull TabPlayer player, @Nullable TabPlayer viewer) {
        player.teamData.setOpaqueNameTagMode(viewer, false);
        if (viewer != null) {
            if (!isOpaqueNameTagMode(player, viewer) && player.teamData.showNametag(viewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION)) {
                updateVisibility(player, viewer);
            }
        } else {
            for (TabPlayer currentViewer : nameTags.getOnlinePlayers().getPlayers()) {
                if (!isOpaqueNameTagMode(player, currentViewer) && player.teamData.showNametag(currentViewer, NameTagInvisibilityReason.OPAQUE_OCCLUSION)) {
                    updateVisibility(player, currentViewer);
                }
            }
        }
    }

    private boolean hasOpaqueNameTagMode(@NonNull TabPlayer player) {
        return player.teamData.hasOpaqueNameTagMode() || !opaqueNameTagViewers.isEmpty();
    }

    private boolean isOpaqueNameTagMode(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
        return player.teamData.isOpaqueNameTagMode(viewer) || opaqueNameTagViewers.contains(viewer);
    }

    private boolean hasLineOfSight(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        if (!viewer.server.canSee(target.server)) return false;
        if (viewer.world != target.world) return false;
        if (!viewer.canSee(target)) return false;
        return TAB.getInstance().getPlatform().hasLineOfSight(viewer, target);
    }

    private static class OpaqueVisibilityCheck {

        @NotNull private final TabPlayer player;
        @NotNull private final TabPlayer viewer;
        private final boolean forceVisible;

        private OpaqueVisibilityCheck(@NotNull TabPlayer player, @NotNull TabPlayer viewer, boolean forceVisible) {
            this.player = player;
            this.viewer = viewer;
            this.forceVisible = forceVisible;
        }
    }

    private static class OpaqueVisibilityResult {

        @NotNull private final TabPlayer player;
        @NotNull private final TabPlayer viewer;
        private final boolean visible;

        private OpaqueVisibilityResult(@NotNull TabPlayer player, @NotNull TabPlayer viewer, boolean visible) {
            this.player = player;
            this.viewer = viewer;
            this.visible = visible;
        }
    }
}
