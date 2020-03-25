package me.neznamy.tab.platforms.bukkit.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.CustomPacketFeature;
import me.neznamy.tab.shared.features.SimpleFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

@SuppressWarnings("deprecation")
public class PerWorldPlayerlist implements SimpleFeature, CustomPacketFeature{

	private boolean allowBypass;
	private List<String> ignoredWorlds;
	private Map<String, List<String>> sharedWorlds;

	@SuppressWarnings("unchecked")
	@Override
	public void load(){
		allowBypass = Configs.advancedconfig.getBoolean("per-world-playerlist.allow-bypass-permission", false);
		ignoredWorlds = Configs.advancedconfig.getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
		sharedWorlds = (Map<String, List<String>>) Configs.advancedconfig.get("per-world-playerlist.shared-playerlist-world-groups");
		for (Player p : Main.getOnlinePlayers()){
			hidePlayer(p);
			showInSameWorldGroup(p);
		}
	}
	@Override
	public void unload(){
		for (Player p : Main.getOnlinePlayers()) for (Player pl : Main.getOnlinePlayers()) p.showPlayer(pl);
	}
	@Override
	public void onJoin(ITabPlayer p) {
		hidePlayer(((TabPlayer)p).player);
		showInSameWorldGroup(((TabPlayer)p).player);
	}
	@Override
	public void onQuit(ITabPlayer p) {
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		hidePlayer(((TabPlayer)p).player);
		showInSameWorldGroup(((TabPlayer)p).player);
	}
	private void showInSameWorldGroup(Player shown){
		Bukkit.getScheduler().runTask(Main.instance, new Runnable() {

			@Override
			public void run() {
				for (Player everyone : Main.getOnlinePlayers()){
					if (everyone == shown) continue;
					if (shouldSee(shown, everyone)) shown.showPlayer(everyone);
					if (shouldSee(everyone, shown)) everyone.showPlayer(shown);
				}
			}
		});
	}
	private boolean shouldSee(Player viewer, Player displayed) {
		if (displayed == viewer) return true;
		String player1WorldGroup = null;
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group).contains(viewer.getWorld().getName())) player1WorldGroup = group;
		}
		String player2WorldGroup = null;
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group).contains(displayed.getWorld().getName())) player2WorldGroup = group;
		}
		if (viewer.getWorld() == displayed.getWorld() || (player1WorldGroup != null && player2WorldGroup != null && player1WorldGroup.equals(player2WorldGroup))) {
			return true;
		}
		if ((allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(viewer.getWorld().getName())) {
			return true;
		}
		return false;
	}
	public void hidePlayer(Player hidden){
		Bukkit.getScheduler().runTask(Main.instance, new Runnable() {

			@Override
			public void run() {
				for (Player everyone : Main.getOnlinePlayers()){
					if (everyone == hidden) continue;
					hidden.hidePlayer(everyone);
					everyone.hidePlayer(hidden);
				}
			}
		});
	}
	//fixing bukkit api bug making players not hide when hidePlayer is called too early
	@Override
	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet) {
		if (packet instanceof PacketPlayOutPlayerInfo) {
			PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
			if (info.action == EnumPlayerInfoAction.ADD_PLAYER) {
				List<PlayerInfoData> toRemove = new ArrayList<PlayerInfoData>();
				for (PlayerInfoData data : info.entries) {
					ITabPlayer added = Shared.getPlayerByTablistUUID(data.uniqueId);
					if (added != null) {
						if (!shouldSee(((TabPlayer)receiver).player, ((TabPlayer)added).player)) toRemove.add(data);
					}
				}
				List<PlayerInfoData> newList = new ArrayList<PlayerInfoData>();
				Arrays.asList(info.entries).forEach(d -> newList.add(d));
				newList.removeAll(toRemove);
				info.entries = newList.toArray(new PlayerInfoData[0]);
				if (info.entries.length == 0) return null;
			}
		}
		return packet;
	}
	@Override
	public String getCPUName() {
		return "PerWorldPlayerlist";
	}
}