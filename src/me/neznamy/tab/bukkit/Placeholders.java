package me.neznamy.tab.bukkit;

import java.util.HashMap;
import java.util.Map.Entry;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;
import com.github.cheesesoftware.PowerfulPermsAPI.PowerfulPermsPlugin;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.MPlayer;

import ch.soolz.xantiafk.xAntiAFKAPI;
import me.clip.deluxetags.DeluxeTag;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import protocolsupport.api.ProtocolSupportAPI;
import us.myles.ViaVersion.api.Via;

public class Placeholders {
	
	public static boolean placeholderAPI;
	public static Essentials essentials;
	public static Economy economy;
	public static Permission perm;
	public static Chat chat;
	public static boolean protocolSupport;
	public static boolean viaVersion;
	public static String noFaction;
	public static String yesFaction;
	public static String noTag;
	public static String yesTag;
	public static String noAfk;
	public static String yesAfk;
	public static String factionsType;
	public static boolean factionsInitialized;
	public static boolean deluxeTags;
	public static PlaceholderAPIExpansion expansion;

	public static ProtocolVersion getVersion(ITabPlayer p){
		try {
			int version;
			if (viaVersion){
				version = Via.getAPI().getPlayerVersion(p.getUniqueId());
				if (version > 0) return ProtocolVersion.fromNumber(version);
			}
			if (protocolSupport){
				version = ProtocolSupportAPI.getProtocolVersion((Player) p.getPlayer()).getId();
				if (version > 0) return ProtocolVersion.fromNumber(version);
			}
		} catch (Exception e) {
			Shared.error("An error occured when getting version of " + p.getName(), e);
		}
		return ProtocolVersion.SERVER_VERSION;
	}
	public static void initialize(){
		try{
			if (Bukkit.getPluginManager().isPluginEnabled("Vault")){
				RegisteredServiceProvider<Economy> rsp1 = Bukkit.getServicesManager().getRegistration(Economy.class);
				if (rsp1 != null) economy = rsp1.getProvider();
				RegisteredServiceProvider<Permission> rsp2 = Bukkit.getServicesManager().getRegistration(Permission.class);
				if (rsp2 != null) perm = rsp2.getProvider();
				RegisteredServiceProvider<Chat> rsp3 = Bukkit.getServicesManager().getRegistration(Chat.class);
				if (rsp3 != null) chat = rsp3.getProvider();
			}
			Main.luckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
			Main.powerfulPerms = (PowerfulPermsPlugin) Bukkit.getPluginManager().getPlugin("PowerfulPerms");
			protocolSupport = Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");
			Main.groupManager = (GroupManager) Bukkit.getPluginManager().getPlugin("GroupManager");
			deluxeTags = Bukkit.getPluginManager().isPluginEnabled("DeluxeTags");
			viaVersion = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
			me.neznamy.tab.shared.Placeholders.relationalPlaceholders = (placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"));
			if (placeholderAPI) PlaceholderAPIExpansion.register();
			essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			Main.pex = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
		} catch (Exception e){
			Shared.error("An error occured when initializing placeholders:", e);
		}
	}
	public static String replace(String s, ITabPlayer p){
		try {
			if (s.contains("%afk%")) s = s.replace("%afk%", isAfk(p)?yesAfk:noAfk);
			if (s.contains("%rank%")) s = s.replace("%rank%", p.getRank());
			s = me.neznamy.tab.shared.Placeholders.setAnimations(s);
			if (s.contains("%afk%")) s = s.replace("%afk%", isAfk(p)?yesAfk:noAfk);
			if (s.contains("%version-group")){
				for (Entry<String, Integer> entry : me.neznamy.tab.shared.Placeholders.online.entrySet()){
					s = s.replace("%version-group:" + entry.getKey()+ "%", entry.getValue()+"");
				}
			}
			s = me.neznamy.tab.shared.Placeholders.setAnimations(s);
			if (placeholderAPI) s = setPlaceholderAPIPlaceholders(s, p);
			s = replaceSimplePlaceholders(s, p);
			if (placeholderAPI) s = setPlaceholderAPIPlaceholders(s, p);
			for (String removed : Configs.removeStrings) {
				if (s.contains(removed)) {
					s = s.replace(removed, "");
				}
				if (s.contains(removed.replace("&", "§"))) {
					s = s.replace(removed.replace("&", "§"), ""); //much more likely to actually match
				}
			}
		} catch (Exception e){
			Shared.error("An error occured when setting placeholders(1) (player: " + p.getName() + ")", e);
		}
		return s;
	}
	public static String setPlaceholderAPIPlaceholders(String s, ITabPlayer p) {
		try {
			return PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), s);
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.error("PlaceholderAPI replace task failed.");
				Shared.error("PlaceholderAPI version: " + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion());
				Shared.error("String to parse: " + s);
				Shared.error("Please send this error to the FIRST author whose name or plugin name you see here:", t);
			} //else now we know why it failed
		}
		return s;
	}
	public static String replaceSimplePlaceholders(String s, ITabPlayer p) {
		try {
			if (s.contains("%money%")) s = s.replace("%money%", p.getMoney());
			if (s.contains("%deluxetag%")) s = s.replace("%deluxetag%", getDeluxeTag(p));
			if (s.contains("%faction%")) s = s.replace("%faction%", getFaction(p));
			if (s.contains("%health%")) s = s.replace("%health%", p.getHealth()+"");
			if (s.contains("%tps%")) s = s.replace("%tps%", Shared.round(Math.min(MethodAPI.getInstance().getTPS(), 20)));
			if (chat != null) {
				if (s.contains("%vault-prefix%") && chat.getPlayerPrefix((Player) p.getPlayer()) != null) s = s.replace("%vault-prefix%", chat.getPlayerPrefix((Player) p.getPlayer()));
				if (s.contains("%vault-suffix%") && chat.getPlayerSuffix((Player) p.getPlayer()) != null) s = s.replace("%vault-suffix%", chat.getPlayerSuffix((Player) p.getPlayer()));
			} else {
				if (s.contains("%vault-prefix%")) s = s.replace("%vault-prefix%", "");
				if (s.contains("%vault-suffix%")) s = s.replace("%vault-suffix%", "");
			}
			if (s.contains("%canseeonline%")){
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				s = s.replace("%canseeonline%", var+"");
			}
			if (s.contains("%canseestaffonline%")){
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				s = s.replace("%canseestaffonline%", var+"");
			}
			if (s.contains("%xPos%")) s = s.replace("%xPos%", ((Player) p.getPlayer()).getLocation().getBlockX()+"");
			if (s.contains("%yPos%")) s = s.replace("%yPos%", ((Player) p.getPlayer()).getLocation().getBlockY()+"");
			if (s.contains("%zPos%")) s = s.replace("%zPos%", ((Player) p.getPlayer()).getLocation().getBlockZ()+"");
			if (s.contains("%displayname%")) s = s.replace("%displayname%", ((Player) p.getPlayer()).getDisplayName());
			if (s.contains("%deaths%")) s = s.replace("%deaths%", ((Player) p.getPlayer()).getStatistic(Statistic.DEATHS)+"");
			if (s.contains("%essentialsnick%")) s = s.replace("%essentialsnick%", p.getNickname());
		} catch (Exception e) {
			Shared.error("An error occured when setting placeholders(2) (player=" + p.getName() + ", online="+((Player) p.getPlayer()).isOnline()+")", e);
		}
		return s;
	}
	@SuppressWarnings("unchecked")
	public static boolean isAfk(ITabPlayer p) {
		try {
			if (Bukkit.getPluginManager().isPluginEnabled("AutoAFK")) {
				me.prunt.autoafk.Main m = (me.prunt.autoafk.Main) Bukkit.getPluginManager().getPlugin("AutoAFK");
				if (((HashMap<Player, Object>) PacketAPI.getField(m, "afkList")).containsKey(p.getPlayer())) return true;
			}
			if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) return xAntiAFKAPI.isAfk((Player) p.getPlayer());
			return (essentials != null && essentials.getUser(p.getUniqueId()) != null && essentials.getUser(p.getUniqueId()).isAfk());
		} catch (Exception e) {
			Shared.error("An error occured when getting AFK status of " + p.getName(), e);
			return false;
		}
	}
	public static String getFaction(ITabPlayer p){
		try {
			if (!factionsInitialized) {
				try {
					Class.forName("com.massivecraft.factions.FPlayers");
					factionsType = "UUID";
				} catch (Exception e) {}
				try {
					Class.forName("com.massivecraft.factions.entity.MPlayer");
					factionsType = "MCore";
				} catch (Exception e) {}
				factionsInitialized = true;
			}
			String name = null;
			if (factionsType == null) return noFaction;
			if (factionsType.equals("UUID")) name = FPlayers.getInstance().getByPlayer((Player) p.getPlayer()).getFaction().getTag();
			if (factionsType.equals("MCore")) name = MPlayer.get(p.getPlayer()).getFactionName();
	        if (name == null || name.length() == 0 || name.contains("Wilderness")) {
	        	return noFaction;
	        }
	        return yesFaction.replace("%value%", name);
		} catch (IllegalStateException e) {
			Shared.error("An error occured when getting faction of a player, was server just /reloaded ?", e);
			return noFaction;
		} catch (Exception e) {
			Shared.error("An error occured when getting faction of " + p.getName(), e);
			return noFaction;
		}
	}
	public static String getDeluxeTag(ITabPlayer p){
		if (deluxeTags) {
			String tag = DeluxeTag.getPlayerDisplayTag((Player) p.getPlayer());
			if (tag == null || tag.equals("")) {
				return noTag;
			}
			return tag;
		} else {
			return noTag;
		}
	}
}