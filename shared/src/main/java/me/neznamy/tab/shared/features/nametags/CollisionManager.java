package me.neznamy.tab.shared.features.nametags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class CollisionManager extends TabFeature {

	private NameTag nametags;
	private boolean collisionRule;
	private Map<TabPlayer, Boolean> collision = new HashMap<>();
	private Map<TabPlayer, Boolean> forcedCollision = new HashMap<>();

	public CollisionManager(NameTag nametags, boolean collisionRule) {
		super(nametags.getFeatureName());
		setRefreshDisplayName("Updating collision");
		this.nametags = nametags;
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
		addUsedPlaceholders(Arrays.asList("%collision%"));
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
		if (forcedCollision.get(player) == collision) return;
		if (collision == null) {
			forcedCollision.remove(player);
		} else {
			forcedCollision.put(player, collision);
		}
		nametags.updateTeamData(player);
	}

	public Boolean getCollisionRule(TabPlayer player) {
		return forcedCollision.get(player);
	}
}