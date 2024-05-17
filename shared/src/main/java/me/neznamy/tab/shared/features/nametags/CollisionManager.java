package me.neznamy.tab.shared.features.nametags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Class managing collision rule for players.
 */
@RequiredArgsConstructor
public class CollisionManager extends TabFeature implements JoinListener, Loadable, Refreshable {

    private final NameTag nameTags;
    private final Condition refreshCondition = Condition.getCondition(config().getString("scoreboard-teams.enable-collision", "true"));

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.COLLISION, 500, p -> {
            TabPlayer player = (TabPlayer) p;
            if (player.teamData.forcedCollision != null) return Boolean.toString(player.teamData.forcedCollision);
            boolean newCollision = !((TabPlayer)p).isDisguised() && refreshCondition.isMet((TabPlayer) p);
            player.teamData.collisionRule = newCollision;
            return Boolean.toString(newCollision);
        });
        addUsedPlaceholder(TabConstants.Placeholder.COLLISION);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            onJoin(all);
        }
    }
    
    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.teamData.collisionRule = refreshCondition.isMet(connectedPlayer);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.teamData.disabled.get()) return;
        nameTags.updateTeamData(p);
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating collision";
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return nameTags.getFeatureName();
    }
}