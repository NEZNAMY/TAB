package me.neznamy.tab.platforms.bukkit.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.shared.ITabPlayer;
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
 * Per-world-playerlist feature. Currently event based, however that causes various (compatibility) issues.
 * Will be reworked to use packets in the future
 */
@SuppressWarnings({"deprecation", "unchecked"})
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
		for (ITabPlayer p : Shared.getPlayers()){
			hidePlayer(p);
			showInSameWorldGroup(p);
		}
	}
	
	@Override
	public void unload(){
		for (ITabPlayer p : Shared.getPlayers()) {
			for (ITabPlayer pl : Shared.getPlayers()) {
				p.getBukkitEntity().showPlayer(pl.getBukkitEntity());
			}
		}
	}
	
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		hidePlayer(connectedPlayer);
		showInSameWorldGroup(connectedPlayer);
	}
	
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		hidePlayer(p);
		showInSameWorldGroup(p);
	}
	
	private void showInSameWorldGroup(ITabPlayer shown){
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				for (ITabPlayer everyone : Shared.getPlayers()){
					if (everyone == shown) continue;
					if (shouldSee(shown, everyone)) shown.getBukkitEntity().showPlayer(everyone.getBukkitEntity());
					if (shouldSee(everyone, shown)) everyone.getBukkitEntity().showPlayer(shown.getBukkitEntity());
				}
			}
		});
	}
	
	private boolean shouldSee(ITabPlayer viewer, ITabPlayer displayed) {
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
		if ((allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(viewer.getWorldName())) {
			return true;
		}
		return false;
	}
	
	private void hidePlayer(ITabPlayer hidden){
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				for (ITabPlayer everyone : Shared.getPlayers()){
					if (everyone == hidden) continue;
					hidden.getBukkitEntity().hidePlayer(everyone.getBukkitEntity());
					everyone.getBukkitEntity().hidePlayer(hidden.getBukkitEntity());
				}
			}
		});
	}
	
	//fixing bukkit api bug making players not hide when hidePlayer is called too early
	@Override
	public PacketPlayOutPlayerInfo onPacketSend(ITabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.action != EnumPlayerInfoAction.ADD_PLAYER) return info;
		List<PlayerInfoData> toRemove = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData data : info.entries) {
			ITabPlayer added = Shared.getPlayerByTablistUUID(data.uniqueId);
			if (added != null && !shouldSee(receiver, added)) {
				toRemove.add(data);
			}
		}
		List<PlayerInfoData> newList = new ArrayList<PlayerInfoData>();
		Arrays.asList(info.entries).forEach(d -> newList.add(d));
		newList.removeAll(toRemove);
		info.entries = newList.toArray(new PlayerInfoData[0]);
		if (info.entries.length == 0) return null;
		return info;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PER_WORLD_PLAYERLIST;
	}
}