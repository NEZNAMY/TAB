package me.neznamy.tab.shared.features.nametags;

import java.util.Collections;
import java.util.WeakHashMap;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class CollisionManager extends TabFeature {

	private final NameTag nameTags;
	private final boolean collisionRule;
	private final WeakHashMap<TabPlayer, Boolean> collision = new WeakHashMap<>();
	private final WeakHashMap<TabPlayer, Boolean> forcedCollision = new WeakHashMap<>();

	public CollisionManager(NameTag nameTags, boolean collisionRule) {
		super(nameTags.getFeatureName(), "Updating collision");
		this.nameTags = nameTags;
		this.collisionRule = collisionRule;
		if (TAB.getInstance().getServerVersion().getMinorVersion() < 9) return; //cannot control collision anyway
		if (!collisionRule) return; //no need to refresh disguise status since collision is disabled
		if (!TAB.getInstance().getPlatform().isPluginEnabled("LibsDisguises") && !TAB.getInstance().getPlatform().isProxy()) return; //no disguise plugin available
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%collision%", 500, p -> {

			if (forcedCollision.containsKey(p)) return forcedCollision.get(p);
			boolean newCollision = !p.isDisguised();
			collision.put(p, newCollision);
			return newCollision;
		});
		addUsedPlaceholders(Collections.singletonList("%collision%"));
	}
	
	public boolean getCollision(TabPlayer p) {
		return forcedCollision.getOrDefault(p, collision.getOrDefault(p, collisionRule));
	}

	@Override
	public void load() {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			collision.put(all, collisionRule);
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