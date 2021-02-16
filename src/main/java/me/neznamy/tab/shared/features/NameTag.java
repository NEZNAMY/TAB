package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.features.types.Refreshable;

public abstract class NameTag implements Feature, Refreshable {

	protected TAB tab;
	protected List<String> usedPlaceholders;
	protected List<String> disabledWorlds;
	protected Set<String> invisiblePlayers = new HashSet<String>();
	public Sorting sorting;

	public NameTag(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.nametag", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		sorting = new Sorting(tab);
	}

	public void startRefreshingTasks() {
		//workaround for a 1.8.x client-sided bug
		tab.getCPUManager().startRepeatingMeasuredTask(500, "refreshing nametag visibility", TabFeature.NAMETAGS, UsageType.REFRESHING_NAMETAG_VISIBILITY, new Runnable() {
			public void run() {
				//nametag visibility
				for (TabPlayer p : tab.getPlayers()) {
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
				
				//collision rule
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
					//cannot control collision rule on <1.9 servers in any way
					for (TabPlayer p : tab.getPlayers()) {
						if (!p.isLoaded() || isDisabledWorld(p.getWorldName())) continue;
						p.updateCollision();
					}
				}
			}
		});
	}

	public boolean isDisabledWorld(String world) {
		return isDisabledWorld(disabledWorlds, world);
	}

	public Set<String> getInvisiblePlayers(){
		return invisiblePlayers;
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
}