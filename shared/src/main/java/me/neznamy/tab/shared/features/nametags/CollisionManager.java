package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.DisguiseListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Class managing collision rule for players.
 */
public class CollisionManager extends RefreshableFeature implements JoinListener, Loadable, CustomThreaded, DisguiseListener {

    @NotNull private final NameTag nameTags;
    @NotNull private final Condition enableCollision;

    /**
     * Constructs new instance.
     *
     * @param   nameTags
     *          Parent feature
     */
    public CollisionManager(@NotNull NameTag nameTags) {
        this.nameTags = nameTags;
        enableCollision = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(nameTags.getConfiguration().getEnableCollision());
    }

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.COLLISION, p -> {
            TabPlayer player = (TabPlayer) p;
            if (player.teamData.forcedCollision != null) return Boolean.toString(player.teamData.forcedCollision);
            boolean newCollision = collision(player);
            player.teamData.collisionRule = newCollision;
            return Boolean.toString(newCollision);
        });
        addUsedPlaceholder(TabConstants.Placeholder.COLLISION);
        for (TabPlayer all : nameTags.getOnlinePlayers().getPlayers()) {
            onJoin(all);
        }
    }
    
    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.teamData.collisionRule = collision(connectedPlayer);
    }

    @Override
    public void onDisguiseStatusChange(@NotNull TabPlayer player) {
        if (player.teamData.forcedCollision != null) return;
        boolean collision = collision(player);
        if (player.teamData.collisionRule == collision) return;
        player.teamData.collisionRule = collision;
        updateCollision(player);
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating collision";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.teamData.isDisabled()) return;
        if (!nameTags.getOnlinePlayers().contains(p)) return; // player is not loaded by this feature yet
        updateCollision(p);
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
     * Updates collision of a player for everyone.
     *
     * @param   player
     *          Player to update collision of
     */
    public void updateCollision(@NotNull TabPlayer player) {
        for (TabPlayer viewer : nameTags.getOnlinePlayers().getPlayers()) {
            if (viewer.teamData.hasTeamRegistered(player)) {
                viewer.getScoreboard().updateTeam(
                        player.teamData.teamName,
                        player.teamData.getCollisionRule() ? Scoreboard.CollisionRule.ALWAYS : Scoreboard.CollisionRule.NEVER
                );
            }
        }
    }

    private boolean collision(TabPlayer player) {
        return !player.isDisguised() && enableCollision.isMet(player);
    }

    public void setCollisionRule(@NotNull me.neznamy.tab.api.TabPlayer player, Boolean collision) {
        ensureActive();
        getCustomThread().execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (Objects.equals(p.teamData.forcedCollision, collision)) return;
            p.teamData.forcedCollision = collision;
            updateCollision(p);
        }, getFeatureName(), "Processing API call (setCollisionRule)"));
    }

    @Nullable
    public Boolean getCollisionRule(@NotNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.forcedCollision;
    }
}
