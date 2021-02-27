package me.neznamy.tab.platforms.bukkit.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Per-world-playerlist feature. Currently bukkit API based, however that causes various (compatibility) issues.
 * Will be reworked to use packets in the future
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerlist implements Loadable, JoinEventListener, WorldChangeListener, PlayerInfoPacketListener{

	private TAB tab;
	private JavaPlugin plugin;
	private boolean allowBypass;
	private List<String> ignoredWorlds;
	private Map<String, List<String>> sharedWorlds;

	public PerWorldPlayerlist(JavaPlugin plugin, TAB tab) {
		this.plugin = plugin;
		this.tab = tab;
	}
	
	@Override
	public void load(){
		allowBypass = tab.getConfiguration().config.getBoolean("per-world-playerlist.allow-bypass-permission", false);
		ignoredWorlds = tab.getConfiguration().config.getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
		sharedWorlds = tab.getConfiguration().config.getConfigurationSection("per-world-playerlist.shared-playerlist-world-groups");
		for (TabPlayer p : tab.getPlayers()){
			hidePlayer(p);
			showInSameWorldGroup(p);
		}
	}
	
	@Override
	public void unload(){
		for (TabPlayer p : tab.getPlayers()) {
			for (TabPlayer pl : tab.getPlayers()) {
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
				try {
					for (TabPlayer everyone : tab.getPlayers()){
						if (everyone == shown) continue;
						if (shouldSee(shown, everyone)) ((Player) shown.getPlayer()).showPlayer((Player) everyone.getPlayer());
						if (shouldSee(everyone, shown)) ((Player) everyone.getPlayer()).showPlayer((Player) shown.getPlayer());
					}
				} catch (Throwable t) {
					tab.getErrorManager().printError("Failed to show players", t);
				}
			}
		});
	}
	
	private boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
		if (displayed == viewer) return true;
		if ((allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(((Player)viewer.getPlayer()).getWorld().getName())) return true;
		String viewerWorldGroup = ((Player)viewer.getPlayer()).getWorld().getName() + "-default"; //preventing unwanted behavior when some group is called exactly like a world
		String targetWorldGroup = ((Player)displayed.getPlayer()).getWorld().getName() + "-default";
		for (String group : sharedWorlds.keySet()) {
			if (sharedWorlds.get(group) != null) {
				if (sharedWorlds.get(group).contains(((Player)viewer.getPlayer()).getWorld().getName())) viewerWorldGroup = group;
				if (sharedWorlds.get(group).contains(((Player)displayed.getPlayer()).getWorld().getName())) targetWorldGroup = group;
			}
		}
		return viewerWorldGroup.equals(targetWorldGroup);
	}
	
	private void hidePlayer(TabPlayer hidden){
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					for (TabPlayer everyone : tab.getPlayers()){
						if (everyone == hidden) continue;
						((Player) hidden.getPlayer()).hidePlayer((Player) everyone.getPlayer());
						((Player) everyone.getPlayer()).hidePlayer((Player) hidden.getPlayer());
					}
				} catch (Throwable t) {
					tab.getErrorManager().printError("Failed to hide players", t);
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
			TabPlayer added = tab.getPlayerByTablistUUID(data.uniqueId);
			if (added != null && !shouldSee(receiver, added)) {
				toRemove.add(data);
			}
		}
		List<PlayerInfoData> newList = new ArrayList<PlayerInfoData>();
		info.entries.forEach(d -> newList.add(d));
		newList.removeAll(toRemove);
		info.entries = newList;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PER_WORLD_PLAYERLIST;
	}
}