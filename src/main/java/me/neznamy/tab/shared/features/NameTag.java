package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.Feature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;

public abstract class NameTag implements Feature, Refreshable {

	protected Set<String> usedPlaceholders;
	protected List<String> disabledWorlds;
	protected List<String> invisiblePlayers = new ArrayList<String>();

	public NameTag() {
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.nametag", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		SortingType.initialize();
	}

	public void startInvisibilityRefreshingTask() {
		//workaround for a 1.8.x client-sided bug
		Shared.cpu.startRepeatingMeasuredTask(500, "refreshing nametag visibility", getFeatureType(), UsageType.REFRESHING_NAMETAG_VISIBILITY, new Runnable() {
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					if (!p.isLoaded() || isDisabledWorld(p.getWorldName())) continue;
					boolean invisible = p.hasInvisibilityPotion();
					if (invisible && !invisiblePlayers.contains(p.getName())) {
						invisiblePlayers.add(p.getName());
						p.updateTeamData();
					}
					if (!invisible && invisiblePlayers.contains(p.getName())) {
						invisiblePlayers.remove(p.getName());
						p.updateTeamData();
					}
				}
			}
		});
	}
	public void startCollisionRefreshingTask() {
		Shared.cpu.startRepeatingMeasuredTask(1000, "refreshing collision", TabFeature.NAMETAGS, UsageType.REFRESHING_COLLISION, new Runnable() {
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					if (!p.isLoaded() || isDisabledWorld(p.getWorldName())) continue;
					boolean collision = getCollision(p);
					if (p.getCollisionRule() != collision) {
						p.setCollisionRule(collision);
						p.updateTeamData();
					}
				}
			}
		});
	}

	private boolean getCollision(TabPlayer p) {
		return !p.isDisguised() && Configs.getCollisionRule(p.getWorldName());
	}

	public boolean isDisabledWorld(String world) {
		return isDisabledWorld(disabledWorlds, world);
	}

	public List<String> getInvisiblePlayers(){
		return invisiblePlayers;
	}

	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
}