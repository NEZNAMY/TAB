package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.LoginPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public class NameTag implements Loadable, Refreshable, LoginPacketListener, QuitEventListener, WorldChangeListener, JoinEventListener {

	protected TAB tab;
	protected Set<String> usedPlaceholders;
	protected List<String> disabledWorlds;
	private boolean collisionRule;
	private List<String> revertedCollision;
	private boolean invisibleNametags;
	protected Set<String> invisiblePlayers = new HashSet<String>();
	public Sorting sorting;
	protected Map<TabPlayer, Boolean> collision = new HashMap<TabPlayer, Boolean>();

	public NameTag(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.nametag", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		collisionRule = tab.getConfiguration().config.getBoolean("enable-collision", true);
		revertedCollision = tab.getConfiguration().config.getStringList("revert-collision-rule-in-" + tab.getPlatform().getSeparatorType()+"s", Arrays.asList("reverted" + tab.getPlatform().getSeparatorType()));
		invisibleNametags = tab.getConfiguration().config.getBoolean("invisible-nametags", false);
		sorting = new Sorting(tab, this);
		usedPlaceholders = new HashSet<>(tab.getConfiguration().config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "tagsuffix"));
		for (TabPlayer p : tab.getPlayers()) {
			usedPlaceholders.addAll(tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(
					p.getProperty("tagprefix").getCurrentRawValue(), p.getProperty("tagsuffix").getCurrentRawValue()));
		}
		tab.debug(String.format("Loaded NameTag feature with parameters collisionRule=%s, revertedCollision=%s, disabledWorlds=%s, invisibleNametags=%s",
				collisionRule, revertedCollision, disabledWorlds, invisibleNametags));
	}
	
	@Override
	public void load(){
		for (TabPlayer all : tab.getPlayers()) {
			((ITabPlayer) all).setTeamName(sorting.getTeamName(all));
			updateProperties(all);
			collision.put(all, true);
			if (all.hasInvisibilityPotion()) invisiblePlayers.add(all.getName());
			if (isDisabledWorld(all.getWorldName())) continue;
			registerTeam(all);
		}
		startRefreshingTasks();
	}
	
	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()) {
			if (!isDisabledWorld(p.getWorldName())) unregisterTeam(p);
		}
	}
	
	public void startRefreshingTasks() {
		//workaround for a 1.8.x client-sided bug
		tab.getCPUManager().startRepeatingMeasuredTask(500, "refreshing nametag visibility", TabFeature.NAMETAGS, UsageType.REFRESHING_NAMETAG_VISIBILITY_AND_COLLISION, new Runnable() {
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
					//cannot control collision rule on <1.9 servers in any way
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) updateCollision(p);
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
		return new ArrayList<>(usedPlaceholders);
	}
	
	public void unregisterTeam(TabPlayer p) {
		if (p.hasTeamHandlingPaused()) return;
		if (p.getTeamName() == null) return;
		for (TabPlayer viewer : tab.getPlayers()) {
			viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(p.getTeamName()).setTeamOptions(69), TabFeature.NAMETAGS);
		}
	}

	public void unregisterTeam(TabPlayer p, TabPlayer viewer) {
		if (p.hasTeamHandlingPaused()) return;
		viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(p.getTeamName()).setTeamOptions(69), TabFeature.NAMETAGS);
	}

	public void registerTeam(TabPlayer p) {
		if (p.hasTeamHandlingPaused()) return;
		Property tagprefix = p.getProperty("tagprefix");
		Property tagsuffix = p.getProperty("tagsuffix");
		for (TabPlayer viewer : tab.getPlayers()) {
			String currentPrefix = tagprefix.getFormat(viewer);
			String currentSuffix = tagsuffix.getFormat(viewer);
			PacketAPI.registerScoreboardTeam(viewer, p.getTeamName(), currentPrefix, currentSuffix, getTeamVisibility(p, viewer), getCollision(p), Arrays.asList(p.getName()), null, TabFeature.NAMETAGS);
		}
	}

	public void registerTeam(TabPlayer p, TabPlayer viewer) {
		if (p.hasTeamHandlingPaused()) return;
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
			((ITabPlayer) p).setTeamName(newName);
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
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() == null || !p.isOnline()) return;
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
		if (!p.isOnline()) return false;
		if (p.getCollisionRule() != null) return p.getCollisionRule();
		if (!collision.containsKey(p)) {
			collision.put(p, revertedCollision.contains(p.getWorldName()) ? !collisionRule : collisionRule);
		}
		return collision.get(p);
	}
	

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		for (TabPlayer all : tab.getPlayers()) {
			if (!all.isLoaded()) continue;
			if (!isDisabledWorld(all.getWorldName())) registerTeam(all, packetReceiver);
		}
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(refreshed.getWorldName())) return;
		boolean refresh;
		if (force) {
			updateProperties(refreshed);
			refresh = true;
		} else {
			boolean prefix = refreshed.getProperty("tagprefix").update();
			boolean suffix = refreshed.getProperty("tagsuffix").update();
			refresh = prefix || suffix;
		}

		if (refresh) updateTeam(refreshed);
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		((ITabPlayer) connectedPlayer).setTeamName(sorting.getTeamName(connectedPlayer));
		updateProperties(connectedPlayer);
		collision.put(connectedPlayer, true);
		for (TabPlayer all : tab.getPlayers()) {
			if (!all.isLoaded()) continue; //avoiding NPE when 2 players join at once
			if (all == connectedPlayer) continue; //already registered 3 lines above
			if (!isDisabledWorld(all.getWorldName())) registerTeam(all, connectedPlayer);
		}
		if (isDisabledWorld(connectedPlayer.getWorldName())) return;
		registerTeam(connectedPlayer);
	}
	
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (!isDisabledWorld(disconnectedPlayer.getWorldName())) unregisterTeam(disconnectedPlayer);
		invisiblePlayers.remove(disconnectedPlayer.getName());
		collision.remove(disconnectedPlayer);
		for (TabPlayer all : tab.getPlayers()) {
			if (all == disconnectedPlayer) continue;
			all.showNametag(disconnectedPlayer.getUniqueId()); //clearing memory from API method
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		updateProperties(p);
		if (isDisabledWorld(to) && !isDisabledWorld(from)) {
			unregisterTeam(p);
		} else if (!isDisabledWorld(to) && isDisabledWorld(from)) {
			registerTeam(p);
		} else {
			updateTeam(p);
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = new HashSet<>(tab.getConfiguration().config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "tagsuffix"));
		for (TabPlayer p : tab.getPlayers()) {
			usedPlaceholders.addAll(tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(
					p.getProperty("tagprefix").getCurrentRawValue(), p.getProperty("tagsuffix").getCurrentRawValue()));
		}
	}

	public void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig("tagprefix");
		p.loadPropertyFromConfig("tagsuffix");
	}
	
	public boolean getTeamVisibility(TabPlayer p, TabPlayer viewer) {
		return !p.hasHiddenNametag() && !p.hasHiddenNametag(viewer.getUniqueId()) && 
			!invisibleNametags && !invisiblePlayers.contains(p.getName());
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGS;
	}
}