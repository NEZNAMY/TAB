package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.Feature;

public abstract class NameTag implements Feature {

	protected List<String> disabledWorlds;
	
	public NameTag() {
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.nametag", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		SortingType.initialize();
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
}