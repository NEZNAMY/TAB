package me.neznamy.tab.shared.features.nametags;

import java.util.Collections;
import java.util.WeakHashMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.feature.JoinListener;
import me.neznamy.tab.api.feature.Loadable;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;

@RequiredArgsConstructor
public class CollisionManager extends TabFeature implements JoinListener, Loadable, Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating collision";
    private final NameTag nameTags;
    private final boolean collisionRule;
    private final WeakHashMap<TabPlayer, Boolean> collision = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, Boolean> forcedCollision = new WeakHashMap<>();

    public boolean getCollision(TabPlayer p) {
        return forcedCollision.getOrDefault(p, collision.getOrDefault(p, collisionRule));
    }

    @Override
    public void load() {
        if (TAB.getInstance().getServerVersion().getMinorVersion() < 9) return; //cannot control collision anyway
        if (!collisionRule) return; //no need to refresh disguise status since collision is disabled
        if (TAB.getInstance().getPlatform().getPluginVersion(TabConstants.Plugin.LIBS_DISGUISES) == null && TAB.getInstance().getServerVersion() != ProtocolVersion.PROXY) return; //no disguise plugin available
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.COLLISION, 500, p -> {

            if (forcedCollision.containsKey(p)) return forcedCollision.get(p);
            boolean newCollision = !p.isDisguised();
            collision.put(p, newCollision);
            return newCollision;
        });
        addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.COLLISION));
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            collision.put(all, true);
        }
    }
    
    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        collision.put(connectedPlayer, collisionRule);
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (nameTags.isDisabledPlayer(p)) return;
        nameTags.updateTeamData(p);
    }
    
    public void setCollisionRule(TabPlayer player, Boolean collision) {
        if (forcedCollision.get(player) == collision) return;
        if (collision == null) {
            forcedCollision.remove(player);
        } else {
            forcedCollision.put(player, collision);
        }
        nameTags.updateTeamData(player);
    }

    public Boolean getCollisionRule(TabPlayer player) {
        return forcedCollision.get(player);
    }
}