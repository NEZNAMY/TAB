package me.neznamy.tab.shared.features.nametags;

import java.util.Objects;
import java.util.WeakHashMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class managing collision rule for players.
 */
@RequiredArgsConstructor
public class CollisionManager extends TabFeature implements JoinListener, Loadable, Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating collision";
    private final NameTag nameTags;
    private final String collisionRule = config().getString("scoreboard-teams.enable-collision", "true");
    private final Condition refreshCondition = Condition.getCondition(collisionRule);
    private final WeakHashMap<TabPlayer, Boolean> collision = new WeakHashMap<>();
    private final WeakHashMap<me.neznamy.tab.api.TabPlayer, Boolean> forcedCollision = new WeakHashMap<>();

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.COLLISION, 500, p -> {
            if (forcedCollision.containsKey(p)) return forcedCollision.get(p);
            boolean newCollision = !((TabPlayer)p).isDisguised() && refreshCondition.isMet((TabPlayer) p);
            collision.put((TabPlayer) p, newCollision);
            return newCollision;
        });
        addUsedPlaceholder(TabConstants.Placeholder.COLLISION);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            collision.put(all, true);
        }
    }
    
    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        collision.put(connectedPlayer, refreshCondition.isMet(connectedPlayer));
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (nameTags.getDisableChecker().isDisabledPlayer(p)) return;
        nameTags.updateTeamData(p);
    }

    /**
     * Returns current collision rule for player.
     *
     * @param   p
     *          Player to get collision rule of
     * @return  Current collision rule for player
     */
    public boolean getCollision(@NotNull TabPlayer p) {
        return forcedCollision.getOrDefault(p, collision.get(p));
    }

    /**
     * Forces collision rule for player. {@code null} removes forced collision.
     *
     * @param   player
     *          Player to force collision rule for
     * @param   collision
     *          Forced collision rule, {@code null} for resetting.
     */
    public void setCollisionRule(@NotNull TabPlayer player, @Nullable Boolean collision) {
        if (Objects.equals(forcedCollision.get(player), collision)) return;
        if (collision == null) {
            forcedCollision.remove(player);
        } else {
            forcedCollision.put(player, collision);
        }
        nameTags.updateTeamData(player);
    }

    /**
     * Returns forced collision rule for specified player. If no collision
     * rule is forced for the player, returns {@code null}.
     *
     * @param   player
     *          Player to get forced collision rule of
     * @return  Forced collision rule of player or {@code null} if not forced
     */
    @Nullable
    public Boolean getCollisionRule(@NotNull TabPlayer player) {
        return forcedCollision.get(player);
    }
}