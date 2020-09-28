package me.neznamy.tab.platforms.bukkit.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Per-world-playerlist feature. Currently bukkit API based, however that causes various (compatibility) issues.
 * Will be reworked to use packets in the future
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerlist implements Loadable, JoinEventListener, WorldChangeListener, PlayerInfoPacketListener{

	private JavaPlugin plugin;
	private boolean allowBypass;
	private List<String> ignoredWorlds;
	private Map<String, List<String>> sharedWorlds;

	public PerWorldPlayerlist(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void load(){
		allowBypass = Configs.advancedconfig.getBoolean("per-world-playerlist.allow-bypass-permission", false);
		ignoredWorlds = Configs.advancedconfig.getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
		sharedWorlds = Configs.advancedconfig.getConfigurationSection("per-world-playerlist.shared-playerlist-world-groups");
		for (TabPlayer p : Shared.getPlayers()){
			hidePlayer(p);
			showInSameWorldGroup(p);
		}
	}
	
	@Override
	public void unload(){
		for (TabPlayer p : Shared.getPlayers()) {
			for (TabPlayer pl : Shared.getPlayers()) {
				((Player) p.getPlayer()).showPlayer((Player) pl.getPlayer());
			}
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		hidePlayer(connectedPlayer);
		showInSameWorldGroup(connectedPlayer);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		hidePlayer(p);
		showInSameWorldGroup(p);
	}
	
	private void showInSameWorldGroup(TabPlayer shown){
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer everyone : Shared.getPlayers()){
					if (everyone == shown) continue;
					if (shouldSee(shown, everyone)) ((Player) shown.getPlayer()).showPlayer((Player) everyone.getPlayer());
					if (shouldSee(everyone, shown)) ((Player) everyone.getPlayer()).showPlayer((Player) shown.getPlayer());
				}
			}
		});
	}
	
	private boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
		if (displayed == viewer) return true;
		String player1WorldGroup = null;
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group).contains(viewer.getWorldName())) player1WorldGroup = group;
		}
		String player2WorldGroup = null;
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group).contains(displayed.getWorldName())) player2WorldGroup = group;
		}
		if (viewer.getWorldName().equals(displayed.getWorldName()) || (player1WorldGroup != null && player2WorldGroup != null && player1WorldGroup.equals(player2WorldGroup))) {
			return true;
		}
		return (allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(viewer.getWorldName());
	}
	
	private void hidePlayer(TabPlayer hidden){
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer everyone : Shared.getPlayers()){
					if (everyone == hidden) continue;
					((Player) hidden.getPlayer()).hidePlayer((Player) everyone.getPlayer());
					((Player) everyone.getPlayer()).hidePlayer((Player) hidden.getPlayer());
				}
			}
		});
	}
	
	//fixing bukkit api bug making players not hide when hidePlayer is called too early
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.action != EnumPlayerInfoAction.ADD_PLAYER) return;
		List<PlayerInfoData> toRemove = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData data : info.entries) {
			TabPlayer added = Shared.getPlayerByTablistUUID(data.uniqueId);
			if (added != null && !shouldSee(receiver, added)) {
				toRemove.add(data);
			}
		}
		List<PlayerInfoData> newList = new ArrayList<PlayerInfoData>();
		info.entries.forEach(d -> newList.add(d));
		newList.removeAll(toRemove);
		info.entries = newList;
	}

	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PER_WORLD_PLAYERLIST;
	}
}