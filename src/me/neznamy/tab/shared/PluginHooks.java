package me.neznamy.tab.shared;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import ch.soolz.xantiafk.xAntiAFKAPI;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.clip.deluxetags.DeluxeTag;
import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.listeners.PlayerListener;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.luckperms.LuckPerms;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import net.alpenblock.bungeeperms.BungeePerms;
import net.lapismc.afkplus.AFKPlus;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
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
	public static boolean deluxetags;
	public static boolean viaversion;
	public static boolean protocolsupport;
	public static boolean ultrapermissions;
	public static Object essentials;
	public static Object idisguise;
	public static Object groupManager;
	public static Object Vault_economy;
	public static Object Vault_permission;
	public static Object Vault_chat;

	public static boolean premiumVanish;

	public static boolean _isVanished(ITabPlayer p) {
		if (p instanceof me.neznamy.tab.platforms.bungee.TabPlayer) {
			if (premiumVanish && BungeeVanishAPI.isInvisible(((me.neznamy.tab.platforms.bungee.TabPlayer)p).player)) return true;
		}
		return false;
	}
	public static boolean AFKPlus_isAFK(ITabPlayer p) {
		return ((AFKPlus)Bukkit.getPluginManager().getPlugin("AFKPlus")).getPlayer(p.getUniqueId()).isAFK();
	}
	//paid plugin and i do not want to leak the jar when providing all dependencies
	public static boolean AntiAFKPlus_isAFK(ITabPlayer p) {
		try {
			Object api = Class.forName("de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI").getDeclaredMethod("getAPI").invoke(null);
			return (boolean) api.getClass().getMethod("isAFK", Player.class).invoke(api, ((TabPlayer)p).player);
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using AntiAFKPlus", t);
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
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using AutoAFK", t);
		}
	}
	public static String BungeePerms_getMainGroup(ITabPlayer p) {
		try {
			return BungeePerms.getInstance().getPermissionsManager().getMainGroup(BungeePerms.getInstance().getPermissionsManager().getUser(p.getUniqueId())).getName();
		} catch (Throwable t) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + p.getName() + " using BungeePerms", t);
		}
	}
	public static String DeluxeTag_getPlayerDisplayTag(ITabPlayer p) {
		return DeluxeTag.getPlayerDisplayTag(((TabPlayer)p).player);
	}
	public static void DeluxeTags_onChat(ITabPlayer p) {
		try {
			if (deluxetags) {
				new PlayerListener((DeluxeTags) Bukkit.getPluginManager().getPlugin("DeluxeTags")).onChat(new AsyncPlayerChatEvent(true, ((TabPlayer)p).player, null, null));
			}
		} catch (Throwable t) {
			//new version which already fixed the issue anyway
		}
	}
	public static double Essentials_getMoney(ITabPlayer p) {
		try {
			return ((Essentials)essentials).getUser(((TabPlayer)p).player).getMoney().doubleValue();
		} catch (Throwable t) {
			return Shared.errorManager.printError(0, "Failed to check money of " + p.getName() + " using Essentials", t);
		}
	}
	public static String Essentials_getNickname(ITabPlayer p) {
		return ((Essentials)essentials).getUser(((TabPlayer)p).player).getNickname();
	}
	public static boolean Essentials_isAFK(ITabPlayer p) {
		try {
			return ((Essentials)essentials).getUser(((TabPlayer)p).player).isAfk();
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using Essentials", t);
		}
	}
	public static String GroupManager_getGroup(ITabPlayer p) {
		try {
			return ((GroupManager)groupManager).getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroup(p.getName());
		} catch (Throwable t) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + p.getName() + " using GroupManager", t);
		}
	}
	public static String[] GroupManager_getGroups(ITabPlayer p) {
		try {
			return ((GroupManager)groupManager).getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroups(p.getName());
		} catch (Throwable t) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using GroupManager", t);
		}
	}
	public static boolean iDisguise_isDisguised(ITabPlayer p) {
		return ((de.robingrether.idisguise.api.DisguiseAPI)idisguise).isDisguised(((TabPlayer)p).player);
	}
	public static boolean LibsDisguises_isDisguised(ITabPlayer p) {
		try {
			return me.neznamy.tab.platforms.bukkit.Main.LibsDisguises_isDisguised(p); //preventing errors on bungee version
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check if player " + p.getName() + " is disguised using LibsDisguises", t);
		}
	}
	public static String[] LuckPerms_getAllGroups(ITabPlayer p) {
		try {
			try {
				//LuckPerms API v5
				User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
					return new String[] {"null"};
				}
				return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				return LuckPerms.getApi().getUser(p.getUniqueId()).getAllNodes().stream().filter(me.lucko.luckperms.api.Node::isGroupNode).map(me.lucko.luckperms.api.Node::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
			}
		} catch (Throwable t) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using LuckPerms", t);
		}
	}
	public static String LuckPerms_getPrefix(ITabPlayer p) {
		try {
			String prefix;
			try {
				//LuckPerms API v5
				User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
					return "";
				}
				prefix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getPrefix();
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				me.lucko.luckperms.api.User user = LuckPerms.getApi().getUser(p.getUniqueId());
				prefix = user.getCachedData().getMetaData(LuckPerms.getApi().getContextManager().getApplicableContexts(p.getClass().getDeclaredField("player").get(p))).getPrefix();
			}
			return prefix == null ? "" : prefix;
		} catch (Throwable t) {
			return Shared.errorManager.printError("", "Failed to get prefix of " + p.getName() + " using LuckPerms", t);
		}
	}
	public static String LuckPerms_getSuffix(ITabPlayer p) {
		try {
			String suffix;
			try {
				//LuckPerms API v5
				User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
					return "";
				}
				suffix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getSuffix();
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				me.lucko.luckperms.api.User user = LuckPerms.getApi().getUser(p.getUniqueId());
				suffix = user.getCachedData().getMetaData(LuckPerms.getApi().getContextManager().getApplicableContexts(p.getClass().getDeclaredField("player").get(p))).getSuffix();
			}
			return suffix == null ? "" : suffix;
		} catch (Throwable t) {
			return Shared.errorManager.printError("", "Failed to get suffix of " + p.getName() + " using LuckPerms", t);
		}
	}
	public static String LuckPerms_getPrimaryGroup(ITabPlayer p) {
		try {
			try {
				//LuckPerms API v5
				User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
					return "null";
				}
				return user.getPrimaryGroup();
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				return LuckPerms.getApi().getUser(p.getUniqueId()).getPrimaryGroup();
			}
		} catch (Throwable t) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + p.getName() + " using LuckPerms", t);
		}
	}
	@SuppressWarnings("deprecation")
	public static String[] PermissionsEx_getGroupNames(ITabPlayer p) {
		try {
			return PermissionsEx.getUser(p.getName()).getGroupNames();
		} catch (Throwable t) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using PermissionsEx", t);
		}
	}
	public static String PlaceholderAPI_setPlaceholders(UUID player, String placeholder) {
		Player p = (player == null ? null : Bukkit.getPlayer(player));
		return PlaceholderAPI_setPlaceholders(p, placeholder);
	}
	public static String PlaceholderAPI_setPlaceholders(Player player, String placeholder) {
		if (!placeholderAPI) return placeholder;
		try {
			return PlaceholderAPI.setPlaceholders(player, placeholder);
		} catch (Throwable t) {
			String playername = (player == null ? "null" : player.getName());
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				if (placeholder.contains("%pinataparty")) {
					//i'm done with arguing with that person about whose fault it is, just pretending like it works
					return "0";
				} else {
					Shared.errorManager.printError("PlaceholderAPI v" + papi.getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, Configs.papiErrorFile);
				}
			} else {
				placeholderAPI = false;
			}
			return "ERROR";
		}
	}
	public static String PlaceholderAPI_setRelationalPlaceholders(ITabPlayer viewer, ITabPlayer target, String text) {
		if (!placeholderAPI) return text;
		try {
			long startTime = System.nanoTime();
			String value = PlaceholderAPI.setRelationalPlaceholders(((TabPlayer)viewer).player, ((TabPlayer)target).player, text);
			Shared.cpu.addPlaceholderTime("PlaceholderAPI-Relational", System.nanoTime()-startTime);
			return value;
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.errorManager.printError("PlaceholderAPI v" + papi.getDescription().getVersion() + " generated an error when setting relational text " + text + " for viewer " + viewer.getName() + " and target " + target.getName(), t);
			} else {
				placeholderAPI = false;
			}
		}
		return text;
	}
	public static int PremiumVanish_getVisiblePlayerCount() {
		return Shared.getPlayers().size() - BungeeVanishAPI.getInvisiblePlayers().size();
	}
	public static int ProtocolSupportAPI_getProtocolVersionId(ITabPlayer p){
		try {
			return ProtocolSupportAPI.getProtocolVersion(((TabPlayer)p).player).getId();
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + p.getName() + " using ProtocolSupport", e);
		}
	}
	public static String[] UltraPermissions_getAllGroups(ITabPlayer p) {
		try {
			UltraPermissionsAPI api = null;
			if (p instanceof me.neznamy.tab.platforms.bungee.TabPlayer) {
				api = UltraPermissionsBungee.getAPI();
			}
			if (p instanceof me.neznamy.tab.platforms.bukkit.TabPlayer) {
				api = UltraPermissions.getAPI();
			}
			if (api == null) return new String[]{"null"};
			List<String> groups = new ArrayList<String>();
			for (Group group : api.getUsers().name(p.getName()).getGroups().bestToWorst().get()) {
				groups.add(group.getName());
			}
			return groups.toArray(new String[0]);
		} catch (Throwable e) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using UltraPermissions", e);
		}
	}
	public static String Vault_getPermissionPlugin() {
		return ((Permission)Vault_permission).getName();
	}
	public static String[] Vault_getGroups(ITabPlayer p) {
		try {
			return ((Permission)Vault_permission).getPlayerGroups(((TabPlayer)p).player);
		} catch (Throwable e) {
			return Shared.errorManager.printError(new String[] {"null"}, "Failed to get permission groups of " + p.getName() + " using Vault", e);
		}
	}
	public static double Vault_getMoney(ITabPlayer p) {
		return me.neznamy.tab.platforms.bukkit.Main.Vault_getMoney(p); //preventing errors on bungee version
	}
	public static String Vault_getPrefix(ITabPlayer p) {
		try {
			return ((Chat)Vault_chat).getPlayerPrefix(((TabPlayer)p).player);
		} catch (Exception e) {
			return Shared.errorManager.printError("", "Failed to get prefix of " + p.getName() + " using Vault", e);
		}
	}
	public static String Vault_getSuffix(ITabPlayer p) {
		try {
			return ((Chat)Vault_chat).getPlayerSuffix(((TabPlayer)p).player);
		} catch (Exception e) {
			return Shared.errorManager.printError("", "Failed to get suffix of " + p.getName() + " using Vault", e);
		}
	}
	public static String Vault_getPrimaryGroup(ITabPlayer p) {
		try {
			return ((Permission)Vault_permission).getPrimaryGroup(((TabPlayer)p).player);
		} catch (Throwable e) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + p.getName() + " using Vault", e);
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
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + p.getName() + " using ViaVersion", e);
		}
	}
	public static boolean xAntiAFK_isAfk(ITabPlayer p) {
		return xAntiAFKAPI.isAfk(((TabPlayer)p).player);
	}
}