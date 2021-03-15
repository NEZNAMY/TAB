package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public abstract class NameTag implements Feature, Refreshable {

	protected TAB tab;
	protected List<String> usedPlaceholders;
	protected List<String> disabledWorlds;
	private boolean collisionRule;
	private List<String> revertedCollision;
	protected Set<String> invisiblePlayers = new HashSet<String>();
	public Sorting sorting;
	protected Map<TabPlayer, Boolean> collision = new HashMap<TabPlayer, Boolean>();

	public NameTag(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.nametag", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		collisionRule = tab.getConfiguration().config.getBoolean("enable-collision", true);
		revertedCollision = tab.getConfiguration().config.getStringList("revert-collision-rule-in-" + tab.getPlatform().getSeparatorType()+"s", Arrays.asList("reverted" + tab.getPlatform().getSeparatorType()));
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
						updateTeamData(p);
					}
					if (!invisible && invisiblePlayers.contains(p.getName())) {
						invisiblePlayers.remove(p.getName());
						updateTeamData(p);
					}
				}
				
				//collision rule
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
					//cannot control collision rule on <1.9 servers in any way
					for (TabPlayer p : tab.getPlayers()) {
						if (!p.isLoaded() || isDisabledWorld(p.getWorldName())) continue;
						updateCollision(p);
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
	
	public abstract boolean getTeamVisibility(TabPlayer p, TabPlayer viewer);
	
	public void unregisterTeam(TabPlayer p) {
		if (p.getTeamName() == null) return;
		for (TabPlayer viewer : tab.getPlayers()) {
			viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(p.getTeamName()).setTeamOptions(69), TabFeature.NAMETAGS);
		}
	}

	public void unregisterTeam(TabPlayer p, TabPlayer viewer) {
		viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(p.getTeamName()).setTeamOptions(69), TabFeature.NAMETAGS);
	}

	public void registerTeam(TabPlayer p) {
		Property tagprefix = p.getProperty("tagprefix");
		Property tagsuffix = p.getProperty("tagsuffix");
		for (TabPlayer viewer : tab.getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			PacketAPI.registerScoreboardTeam(viewer, p.getTeamName(), currentPrefix, currentSuffix, getTeamVisibility(p, viewer), getCollision(p), Arrays.asList(p.getName()), null, TabFeature.NAMETAGS);
		}
	}

	public void registerTeam(TabPlayer p, TabPlayer viewer) {
		Property tagprefix = p.getProperty("tagprefix");
		Property tagsuffix = p.getProperty("tagsuffix");
		String replacedPrefix = tagprefix.getFormat(viewer);
		String replacedSuffix = tagsuffix.getFormat(viewer);
		PacketAPI.registerScoreboardTeam(viewer, p.getTeamName(), replacedPrefix, replacedSuffix, getTeamVisibility(p, viewer), getCollision(p), Arrays.asList(p.getName()), null, TabFeature.NAMETAGS);
	}

	public void updateTeam(TabPlayer p) {
		if (p.getTeamName() == null) return; //player not loaded yet
		String newName = sorting.getTeamName(p);
		if (p.getTeamName().equals(newName)) {
			updateTeamData(p);
		} else {
			unregisterTeam(p);
			p.setTeamName(newName);
			registerTeam(p);
		}
	}

	public void updateTeamData(TabPlayer p) {
		Property tagprefix = p.getProperty("tagprefix");
		Property tagsuffix = p.getProperty("tagsuffix");
		for (TabPlayer viewer : tab.getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			boolean visible = getTeamVisibility(p, viewer);
			viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(p.getTeamName(), currentPrefix, currentSuffix, visible?"always":"never", getCollision(p)?"always":"never", 69), TabFeature.NAMETAGS);
		}
	}

	public void updateTeamData(TabPlayer p, TabPlayer viewer) {
		Property tagprefix = p.getProperty("tagprefix");
		Property tagsuffix = p.getProperty("tagsuffix");
		boolean visible = getTeamVisibility(p, viewer);
		String currentPrefix = tagprefix.getFormat(viewer);
		String currentSuffix = tagsuffix.getFormat(viewer);
		viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(p.getTeamName(), currentPrefix, currentSuffix, visible?"always":"never", getCollision(p)?"always":"never", 69), TabFeature.NAMETAGS);
	}
	
	private void updateCollision(TabPlayer p) {
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() == null) return;
		if (p.getCollisionRule() != null) {
			if (getCollision(p) != p.getCollisionRule()) {
				collision.put(p, p.getCollisionRule());
				updateTeamData(p);
			}
		} else {
			boolean collision = !p.isDisguised() && revertedCollision.contains(p.getWorldName()) ? !collisionRule : collisionRule;
			if (this.collision.get(p) == null || this.getCollision(p) != collision) {
				this.collision.put(p, collision);
				updateTeamData(p);
			}
		}
	}
	
	protected boolean getCollision(TabPlayer p) {
		if (p.getCollisionRule() != null) return p.getCollisionRule();
		if (!collision.containsKey(p)) {
			collision.put(p, revertedCollision.contains(p.getWorldName()) ? !collisionRule : collisionRule);
		}
		return collision.get(p);
	}
}