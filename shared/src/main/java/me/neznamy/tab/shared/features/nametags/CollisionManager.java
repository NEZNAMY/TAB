package me.neznamy.tab.shared.features.nametags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class CollisionManager extends TabFeature {

	private NameTag nametags;
	
	private Map<TabPlayer, Boolean> collision = new HashMap<>();
	private Map<TabPlayer, Boolean> forcedCollision = new HashMap<>();

	public CollisionManager(NameTag nametags, boolean collisionRule) {
		super(nametags.getFeatureName());
		this.nametags = nametags;
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%collision%", 500, p -> {

			if (forcedCollision.containsKey(p)) return forcedCollision.get(p);
			boolean newCollision = !p.isDisguised() && collisionRule;
			collision.put(p, newCollision);
			return newCollision;
		});
		addUsedPlaceholders(Arrays.asList("%collision%"));
	}
	
	public boolean getCollision(TabPlayer p) {
		if (forcedCollision.get(p) != null) return forcedCollision.get(p);
		if (!collision.containsKey(p)) return true;
		return collision.get(p);
	}

	@Override
	public void load() {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			collision.put(all, true);
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		collision.put(connectedPlayer, true);
	}
	
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		collision.remove(disconnectedPlayer);
		forcedCollision.remove(disconnectedPlayer);
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (nametags.isDisabledPlayer(p)) return;
		nametags.updateTeamData(p);
	}
	
	public void setCollisionRule(TabPlayer player, Boolean collision) {
		if (collision == null) {
			forcedCollision.remove(player);
		} else {
			forcedCollision.put(player, collision);
		}
	}

	public Boolean getCollisionRule(TabPlayer player) {
		return forcedCollision.get(player);
	}
}