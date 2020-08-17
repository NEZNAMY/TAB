package me.neznamy.tab.shared.features;

import java.util.Set;

import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

public class NameTag16 implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, Refreshable{

	private Set<String> usedPlaceholders;
	private boolean fixVisibility;
	
	public NameTag16(boolean fixVisibility) {
		this.fixVisibility = fixVisibility;
		refreshUsedPlaceholders();
	}
	@Override
	public void load(){
		for (ITabPlayer p : Shared.getPlayers()) {
			p.teamName = SortingType.INSTANCE.getTeamName(p);
			updateProperties(p);
			if (!p.disabledNametag) p.registerTeam();
		}
		//fixing a 1.8.x client-sided vanilla bug on bukkit mode
		if (fixVisibility) {
			for (ITabPlayer p : Shared.getPlayers()) {
				p.nameTagVisible = !p.hasInvisibility();
			}
			Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", CPUFeature.NAMETAG_INVISFIX, new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) {
						boolean visible = !p.hasInvisibility();
						if (p.nameTagVisible != visible) {
							p.nameTagVisible = visible;
							p.updateTeamData();
						}
					}
				}
			});
		}
		Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing collision", CPUFeature.NAMETAG_COLLISION, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.onJoinFinished) continue;
					boolean collision = p.getTeamPush();
					if (p.lastCollision != collision) {
						p.updateTeamData();
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.unregisterTeam();
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		connectedPlayer.teamName = SortingType.INSTANCE.getTeamName(connectedPlayer);
		updateProperties(connectedPlayer);
		if (connectedPlayer.disabledNametag) return;
		connectedPlayer.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue; //already registered 2 lines above
			if (!all.disabledNametag) all.registerTeam(connectedPlayer);
		}
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		if (!disconnectedPlayer.disabledNametag) disconnectedPlayer.unregisterTeam();
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		updateProperties(p);
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam();
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			if (Shared.platform.getSeparatorType().equals("server")) {
				Shared.featureCpu.runTaskLater(500, "refreshing nametags", CPUFeature.NAMETAG, new Runnable() {

					@Override
					public void run() {
						p.unregisterTeam();
						p.registerTeam();
					}
				});
			} else {
				p.updateTeam();
			}
		}
	}
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (refreshed.disabledNametag) return;
		boolean refresh;
		if (force) {
			updateProperties(refreshed);
			refresh = true;
		} else {
			boolean prefix = refreshed.properties.get("tagprefix").update();
			boolean suffix = refreshed.properties.get("tagsuffix").update();
			refresh = prefix || suffix;
		}
		
		if (refresh) refreshed.updateTeam();
	}
	private void updateProperties(ITabPlayer p) {
		p.updateProperty("tagprefix");
		p.updateProperty("tagsuffix");
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.NAMETAG;
	}
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "tagsuffix");
	}
}