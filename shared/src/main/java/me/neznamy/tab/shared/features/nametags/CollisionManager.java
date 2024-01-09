package me.neznamy.tab.shared.features.nametags;

import java.util.Objects;
import java.util.WeakHashMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class CollisionManager extends TabFeature implements JoinListener, Loadable, Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating collision";
    private final NameTag nameTags;
    private final boolean collisionRule;
    private final WeakHashMap<TabPlayer, Boolean> collision = new WeakHashMap<>();
    private final WeakHashMap<me.neznamy.tab.api.TabPlayer, Boolean> forcedCollision = new WeakHashMap<>();

    public boolean getCollision(TabPlayer p) {
        return forcedCollision.getOrDefault(p, collision.getOrDefault(p, collisionRule));
    }

    @Override
    public void load() {
        if (TAB.getInstance().getServerVersion().getMinorVersion() < 9) return; //cannot control collision anyway
        if (!collisionRule) return; //no need to refresh disguise status since collision is disabled
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.COLLISION, 500, p -> {

            if (forcedCollision.containsKey(p)) return forcedCollision.get(p);
            boolean newCollision = !((TabPlayer)p).isDisguised();
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
        collision.put(connectedPlayer, collisionRule);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (nameTags.getDisableChecker().isDisabledPlayer(p)) return;
        nameTags.updateTeamData(p);
    }
    
    public void setCollisionRule(@NotNull TabPlayer player, @Nullable Boolean collision) {
        if (Objects.equals(forcedCollision.get(player), collision)) return;
        if (collision == null) {
            forcedCollision.remove(player);
        } else {
            forcedCollision.put(player, collision);
        }
        nameTags.updateTeamData(player);
    }

    public @Nullable Boolean getCollisionRule(@NotNull TabPlayer player) {
        return forcedCollision.get(player);
    }
}