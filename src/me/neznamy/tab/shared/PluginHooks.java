package me.neznamy.tab.shared;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.MPlayer;

import ch.soolz.xantiafk.xAntiAFKAPI;
import me.clip.deluxetags.DeluxeTag;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.luckperms.LuckPerms;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import net.alpenblock.bungeeperms.BungeePerms;
import net.lapismc.afkplus.AFKPlus;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import protocolsupport.api.ProtocolSupportAPI;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.myles.ViaVersion.api.Via;

@SuppressWarnings({"rawtypes"})
public class PluginHooks {

	public static boolean libsDisguises;
	public static boolean luckPerms;
	public static boolean permissionsEx;
	public static boolean placeholderAPI;
	public static Object essentials;
	public static Object idisguise;
	public static Object groupManager;
	public static Object Vault_economy;
	public static Object Vault_permission;
	public static Object Vault_chat;

	public static boolean AFKPlus_isAFK(ITabPlayer p) {
		return ((AFKPlus)Bukkit.getPluginManager().getPlugin("AFKPlus")).getPlayer(p.getUniqueId()).isAFK();
	}
	//paid plugin and i do not want to leak the jar when providing all dependencies
	public static boolean AntiAFKPlus_isAFK(ITabPlayer p) {
		try {
			Object api = Class.forName("de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI").getDeclaredMethod("getAPI").invoke(null);
			return (boolean) api.getClass().getMethod("isAFK", Player.class).invoke(api, ((TabPlayer)p).player);
		} catch (Throwable t) {
			return Shared.error(false, "Failed to check AFK status of " + p.getName() + " using AntiAFKPlus", t);
		}
	}
	//map is private
	public static boolean AutoAFK_isAFK(ITabPlayer p) {
		try {
			Object plugin = Bukkit.getPluginManager().getPlugin("AutoAFK");
			Field f = plugin.getClass().getDeclaredField("afkList");
			f.setAccessible(true);
			HashMap map = (HashMap) f.get(plugin);
			return map.containsKey(((TabPlayer)p).player);
		} catch (Throwable t) {
			return Shared.error(false, "Failed to check AFK status of " + p.getName() + " using AutoAFK", t);
		}
	}
	public static String BungeePerms_getMainGroup(ITabPlayer p) {
		try {
			return BungeePerms.getInstance().getPermissionsManager().getMainGroup(BungeePerms.getInstance().getPermissionsManager().getUser(p.getUniqueId())).getName();
		} catch (Throwable t) {
			return Shared.error("null", "Failed to get permission group of " + p.getName() + " using BungeePerms", t);
		}
	}
	public static String DeluxeTag_getPlayerDisplayTag(ITabPlayer p) {
		return DeluxeTag.getPlayerDisplayTag(((TabPlayer)p).player);
	}
	public static double Essentials_getMoney(ITabPlayer p) {
		try {
			return ((Essentials)essentials).getUser(((TabPlayer)p).player).getMoney().doubleValue();
		} catch (Throwable t) {
			return Shared.error(0, "Failed to check money of " + p.getName() + " using Essentials", t);
		}
	}
	public static String Essentials_getNickname(ITabPlayer p) {
		return ((Essentials)essentials).getUser(((TabPlayer)p).player).getNickname();
	}
	public static boolean Essentials_isAFK(ITabPlayer p) {
		try {
			return ((Essentials)essentials).getUser(((TabPlayer)p).player).isAfk();
		} catch (Throwable t) {
			return Shared.error(false, "Failed to check AFK status of " + p.getName() + " using Essentials", t);
		}
	}
	public static String FactionsMCore_getFactionName(ITabPlayer p) {
		return MPlayer.get(((TabPlayer)p).player).getFactionName();
	}
	public static String FactionsUUID_getFactionTag(ITabPlayer p) {
		return FPlayers.getInstance().getByPlayer(((TabPlayer)p).player).getFaction().getTag();
	}
	public static String GroupManager_getGroup(ITabPlayer p) {
		try {
			return ((GroupManager)groupManager).getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroup(p.getName());
		} catch (Throwable t) {
			return Shared.error("null", "Failed to get permission group of " + p.getName() + " using GroupManager", t);
		}
	}
	public static String[] GroupManager_getGroups(ITabPlayer p) {
		try {
			return ((GroupManager)groupManager).getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroups(p.getName());
		} catch (Throwable t) {
			return Shared.error(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using GroupManager", t);
		}
	}
	public static boolean iDisguise_isDisguised(ITabPlayer p) {
		return ((de.robingrether.idisguise.api.DisguiseAPI)idisguise).isDisguised(((TabPlayer)p).player);
	}
	public static boolean LibsDisguises_isDisguised(ITabPlayer p) {
		try {
			return me.neznamy.tab.platforms.bukkit.Main.LibsDisguises_isDisguised(p); //preventing errors on bungee version
		} catch (Throwable t) {
			return Shared.error(false, "Failed to check if player " + p.getName() + " is disguised using LibsDisguises", t);
		}
	}
	public static String[] LuckPerms_getAllGroups(ITabPlayer p) {
		try {
			try {
				//LuckPerms API v5
				return LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId()).getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				return LuckPerms.getApi().getUser(p.getUniqueId()).getAllNodes().stream().filter(me.lucko.luckperms.api.Node::isGroupNode).map(me.lucko.luckperms.api.Node::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
			}
		} catch (Throwable t) {
			return Shared.error(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using LuckPerms", t);
		}
	}
	public static String LuckPerms_getPrimaryGroup(ITabPlayer p) {
		try {
			try {
				//LuckPerms API v5
				return LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId()).getPrimaryGroup();
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				return LuckPerms.getApi().getUser(p.getUniqueId()).getPrimaryGroup();
			}
		} catch (Throwable t) {
			return Shared.error("null", "Failed to get permission group of " + p.getName() + " using LuckPerms", t);
		}
	}
	@SuppressWarnings("deprecation")
	public static String[] PermissionsEx_getGroupNames(ITabPlayer p) {
		try {
			return PermissionsEx.getUser(((TabPlayer)p).player).getGroupNames();
		} catch (Throwable t) {
			return Shared.error(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using PermissionsEx", t);
		}
	}
	public static String PlaceholderAPI_setPlaceholders(ITabPlayer p, String s, String[] placeholders, boolean logCpu) {
		if (!placeholderAPI) return s;
		Player player = (p == null ? null : ((TabPlayer)p).player);
		try {
			long startTime = System.nanoTime();
			String value = PlaceholderAPI.setPlaceholders(player, s);
			if (logCpu) Shared.placeholderCpu("PlaceholderAPI" + Arrays.toString(placeholders), System.nanoTime()-startTime);
			return value;
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				if (s.contains("%pinataparty")) {
					//i'm done with arguing with that person about whose fault it is, just pretending like it works
					return s.replace("%pinataparty_votes_total%", "0");
				} else {
					Shared.error(null, "PlaceholderAPI replace task failed. PlaceholderAPI version: " + papi.getDescription().getVersion() + ". Placeholders to replace: " + Arrays.toString(placeholders));
					Shared.error(null, "Input arguments: [Player = " + player + ", String = " + s + "]");
					Shared.error(null, "Please send this error to the FIRST author whose name or plugin name you see here:", t);
				}
			} else {
				//thats why it failed
				placeholderAPI = false;
			}
		}
		return s;
	}
	public static String PlaceholderAPI_setRelationalPlaceholders(ITabPlayer one, ITabPlayer two, String s) {
		try {
			if (placeholderAPI) {
				long startTime = System.nanoTime();
				String value = PlaceholderAPI.setRelationalPlaceholders(((TabPlayer)one).player, ((TabPlayer)two).player, s);
				Shared.placeholderCpu("PlaceholderAPI-Relational", System.nanoTime()-startTime);
				return value;
			}
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.error(null, "Relational PlaceholderAPI replace task failed. PlaceholderAPI version: " + papi.getDescription().getVersion());
				Shared.error(null, "String to replace: " + s);
				Shared.error(null, "Please send this error to the FIRST author whose name or plugin name you see here:", t);
			} else {
				//thats why it failed
				placeholderAPI = false;
			}
		}
		return s;
	}
	public static int ProtocolSupportAPI_getProtocolVersionId(ITabPlayer p){
		try {
			return ProtocolSupportAPI.getProtocolVersion(((TabPlayer)p).player).getId();
		} catch (Throwable e) {
			return Shared.error(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + p.getName() + " using ProtocolSupport", e);
		}
	}
	public static String Vault_getPermissionPlugin() {
		return ((Permission)Vault_permission).getName();
	}
	public static String[] Vault_getGroups(ITabPlayer p) {
		try {
			return ((Permission)Vault_permission).getPlayerGroups(((TabPlayer)p).player);
		} catch (Throwable e) {
			return Shared.error(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using Vault", e);
		}
	}
	public static double Vault_getMoney(ITabPlayer p) {
		return me.neznamy.tab.platforms.bukkit.Main.Vault_getMoney(p); //preventing errors on bungee version
	}
	public static String Vault_getPrimaryGroup(ITabPlayer p) {
		try {
			return ((Permission)Vault_permission).getPrimaryGroup(((TabPlayer)p).player);
		} catch (Throwable e) {
			return Shared.error("null", "Failed to get permission group of " + p.getName() + " using Vault", e);
		}
	}
	public static void Vault_loadProviders() {
		RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rspEconomy != null) Vault_economy = rspEconomy.getProvider();
		RegisteredServiceProvider<Permission> rspPermission = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (rspPermission != null) Vault_permission = rspPermission.getProvider();
		RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
		if (rspChat != null) Vault_chat = rspChat.getProvider();
	}
	public static int ViaVersion_getPlayerVersion(ITabPlayer p){
		try {
			return Via.getAPI().getPlayerVersion(p.getUniqueId());
		} catch (Throwable e) {
			return Shared.error(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + p.getName() + " using ViaVersion", e);
		}
	}
	public static boolean xAntiAFK_isAfk(ITabPlayer p) {
		return xAntiAFKAPI.isAfk(((TabPlayer)p).player);
	}
}