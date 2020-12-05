package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.LoginPacketListener;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

/**
 * Feature handler for nametag feature
 */
public class NameTag16 extends NameTag implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, LoginPacketListener {

	public NameTag16() {
		refreshUsedPlaceholders();
	}

	@Override
	public void load(){
		for (TabPlayer p : Shared.getPlayers()) {
			p.setTeamName(SortingType.INSTANCE.getTeamName(p));
			updateProperties(p);
			if (p.hasInvisibilityPotion()) {
				invisiblePlayers.add(p.getName());
			}
			if (!isDisabledWorld(p.getWorldName())) p.registerTeam();
		}
		startRefreshingTasks();
	}

	@Override
	public void unload() {
		for (TabPlayer p : Shared.getPlayers()) {
			if (!isDisabledWorld(p.getWorldName())) p.unregisterTeam();
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setTeamName(SortingType.INSTANCE.getTeamName(connectedPlayer));
		updateProperties(connectedPlayer);
		for (TabPlayer all : Shared.getPlayers()) {
			if (!all.isLoaded()) continue; //avoiding NPE when 2 players join at once
			if (all == connectedPlayer) continue; //already registered 3 lines above
			if (!isDisabledWorld(all.getWorldName())) all.registerTeam(connectedPlayer);
		}
		if (isDisabledWorld(connectedPlayer.getWorldName())) return;
		connectedPlayer.registerTeam();
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (!isDisabledWorld(disconnectedPlayer.getWorldName())) disconnectedPlayer.unregisterTeam();
		invisiblePlayers.remove(disconnectedPlayer.getName());
		for (TabPlayer all : Shared.getPlayers()) {
			if (all == disconnectedPlayer) continue;
			all.showNametag(disconnectedPlayer.getUniqueId()); //clearing memory from API method
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		updateProperties(p);
		if (isDisabledWorld(p.getWorldName()) && !isDisabledWorld(from)) {
			p.unregisterTeam();
		} else if (!isDisabledWorld(p.getWorldName()) && isDisabledWorld(from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
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

		if (refresh) refreshed.updateTeam();
	}

	private void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig("tagprefix");
		p.loadPropertyFromConfig("tagsuffix");
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "tagsuffix");
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGS;
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		for (TabPlayer all : Shared.getPlayers()) {
			if (!all.isLoaded()) continue;
			if (!isDisabledWorld(all.getWorldName())) all.registerTeam(packetReceiver);
		}
	}
}