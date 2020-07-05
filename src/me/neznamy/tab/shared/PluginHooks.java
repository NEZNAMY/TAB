package me.neznamy.tab.shared;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.luckperms.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

public class PluginHooks {

	public static boolean libsDisguises;
	public static boolean placeholderAPI;
	public static boolean deluxetags;
	public static boolean viaversion;
	public static boolean protocolsupport;
	public static Object essentials;
	public static Object idisguise;
	public static Object groupManager;
	public static Object Vault_economy;
	public static Object Vault_chat;

	public static boolean premiumVanish;

	public static boolean _isVanished(ITabPlayer p) {
		if (p instanceof me.neznamy.tab.platforms.bungee.TabPlayer) {
			try {
				if (premiumVanish && BungeeVanishAPI.isInvisible(p.getBungeeEntity())) return true;
			} catch (Throwable t) {
				return Shared.errorManager.printError(false, "Failed to check Vanish status of " + p.getName() + " using PremiumVanish", t);
			}
		}
		return false;
	}
	public static String DeluxeTag_getPlayerDisplayTag(ITabPlayer p) {
		try {
			return (String) Class.forName("me.clip.deluxetags.DeluxeTag").getMethod("getPlayerDisplayTag", Player.class).invoke(null, p.getBukkitEntity());
		} catch (Throwable t) {
			return Shared.errorManager.printError("", "Failed to get DeluxeTag of " + p.getName(), t);
		}
	}
	public static double Essentials_getMoney(ITabPlayer p) {
		try {
			return ((Essentials)essentials).getUser(p.getBukkitEntity()).getMoney().doubleValue();
		} catch (Throwable t) {
			return Shared.errorManager.printError(0, "Failed to check money of " + p.getName() + " using Essentials", t);
		}
	}
	public static String Essentials_getNickname(ITabPlayer p) {
		return ((Essentials)essentials).getUser(p.getBukkitEntity()).getNickname();
	}
	public static boolean iDisguise_isDisguised(ITabPlayer p) {
		try {
			return ((de.robingrether.idisguise.api.DisguiseAPI)idisguise).isDisguised(p.getBukkitEntity());
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check disguise status of " + p.getName() + " using iDisguise", t);
		}
	}
	public static boolean LibsDisguises_isDisguised(ITabPlayer p) {
		try {
			return (boolean) Class.forName("me.libraryaddict.disguise.DisguiseAPI").getMethod("isDisguised", Entity.class).invoke(null, p.getBukkitEntity());
		} catch (Exception e) {
			return Shared.errorManager.printError(false, "Failed to check disguise status of " + p.getName() + " using LibsDisguises", e);
		}
	}
	public static String LuckPerms_getPrefix(ITabPlayer p) {
		try {
			String prefix;
			try {
				//LuckPerms API v5
				User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
					return "";
				}
				prefix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getPrefix();
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				me.lucko.luckperms.api.User user = LuckPerms.getApi().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
					return "";
				}
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
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
					return "";
				}
				suffix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getSuffix();
			} catch (NoClassDefFoundError e) {
				//LuckPerms API v4
				me.lucko.luckperms.api.User user = LuckPerms.getApi().getUser(p.getUniqueId());
				if (user == null) {
					Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
					return "";
				}
				suffix = user.getCachedData().getMetaData(LuckPerms.getApi().getContextManager().getApplicableContexts(p.getClass().getDeclaredField("player").get(p))).getSuffix();
			}
			return suffix == null ? "" : suffix;
		} catch (Throwable t) {
			return Shared.errorManager.printError("", "Failed to get suffix of " + p.getName() + " using LuckPerms", t);
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
				Shared.errorManager.printError("PlaceholderAPI v" + papi.getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, Configs.papiErrorFile);
			} else {
				placeholderAPI = false;
			}
			return "ERROR";
		}
	}
	public static String PlaceholderAPI_setRelationalPlaceholders(ITabPlayer viewer, ITabPlayer target, String placeholder) {
		if (!placeholderAPI) return placeholder;
		try {
			return PlaceholderAPI.setRelationalPlaceholders(viewer.getBukkitEntity(), target.getBukkitEntity(), placeholder);
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.errorManager.printError("PlaceholderAPI v" + papi.getDescription().getVersion() + " generated an error when setting relational placeholder " + placeholder + " for viewer " + viewer.getName() + " and target " + target.getName(), t, false, Configs.papiErrorFile);
			} else {
				placeholderAPI = false;
			}
		}
		return placeholder;
	}
	public static int PremiumVanish_getVisiblePlayerCount() {
		try {
			return Shared.getPlayers().size() - BungeeVanishAPI.getInvisiblePlayers().size();
		} catch (Throwable t) {
			return Shared.errorManager.printError(Shared.getPlayers().size(), "Failed to get invisible player count using PremiumVanish");
		}
	}
	public static int ProtocolSupportAPI_getProtocolVersionId(ITabPlayer p){
		try {
			Object protocolVersion = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class).invoke(null, p.getBukkitEntity());
			int ver = (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
			Shared.debug("ProtocolSupport returned protocol version " + ver + " for player " + p.getName());
			return ver;
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + p.getName() + " using ProtocolSupport v" + getVersion("ProtocolSupport"), e);
		}
	}
	public static double Vault_getMoney(ITabPlayer p) {
		try {
			return me.neznamy.tab.platforms.bukkit.Main.instance.Vault_getMoney(p); //preventing errors on bungee version
		} catch (Throwable e) {
			return Shared.errorManager.printError(0, "Failed to get money of " + p.getName() + " using Vault", e);
		}
	}
	public static String Vault_getPrefix(ITabPlayer p) {
		try {
			return ((Chat)Vault_chat).getPlayerPrefix(p.getBukkitEntity());
		} catch (Exception e) {
			return Shared.errorManager.printError("", "Failed to get prefix of " + p.getName() + " using Vault", e);
		}
	}
	public static String Vault_getSuffix(ITabPlayer p) {
		try {
			return ((Chat)Vault_chat).getPlayerSuffix(p.getBukkitEntity());
		} catch (Exception e) {
			return Shared.errorManager.printError("", "Failed to get suffix of " + p.getName() + " using Vault", e);
		}
	}
	public static void Vault_loadProviders() {
		RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rspEconomy != null) Vault_economy = rspEconomy.getProvider();
		RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
		if (rspChat != null) Vault_chat = rspChat.getProvider();
	}
	public static int ViaVersion_getPlayerVersion(ITabPlayer p){
		try {
			Object viaAPI = Class.forName("us.myles.ViaVersion.api.Via").getMethod("getAPI").invoke(null);
			int ver = (int) viaAPI.getClass().getMethod("getPlayerVersion", UUID.class).invoke(viaAPI, p.getUniqueId());
			Shared.debug("ViaVersion returned protocol version " + ver + " for player " + p.getName());
			return ver;
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + p.getName() + " using ViaVersion v" + getVersion("ViaVersion"), e);
		}
	}

	private static String getVersion(String bukkitPlugin) {
		Plugin plugin = Bukkit.getPluginManager().getPlugin(bukkitPlugin);
		if (plugin != null) {
			return plugin.getDescription().getVersion();
		} else {
			return "-1";
		}
	}
}