package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.MPlayer;

import ch.soolz.xantiafk.xAntiAFKAPI;
import me.clip.deluxetags.DeluxeTag;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LocalizedNode;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import net.alpenblock.bungeeperms.BungeePerms;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import protocolsupport.api.ProtocolSupportAPI;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.myles.ViaVersion.api.Via;

public class PluginHooks {

	public static boolean libsDisguises;
	public static boolean luckPerms;
	public static boolean permissionsEx;
	public static boolean placeholderAPI;
	public static Essentials essentials;
	public static de.robingrether.idisguise.api.DisguiseAPI idisguise;
	public static Economy Vault_economy;
	public static GroupManager groupManager;
	public static Permission Vault_permission;

	public static String BungeePerms_getMainGroup(ITabPlayer p) {
		return BungeePerms.getInstance().getPermissionsManager().getMainGroup(BungeePerms.getInstance().getPermissionsManager().getUser(p.getUniqueId())).getName();
	}
	public static String DeluxeTag_getPlayerDisplayTag(ITabPlayer p) {
		return DeluxeTag.getPlayerDisplayTag(((TabPlayer)p).player);
	}
	public static String FactionsMCore_getFactionName(ITabPlayer p) {
		return MPlayer.get(((TabPlayer)p).player).getFactionName();
	}
	public static String FactionsUUID_getFactionTag(ITabPlayer p) {
		return FPlayers.getInstance().getByPlayer(((TabPlayer)p).player).getFaction().getTag();
	}
	public static String GroupManager_getGroup(ITabPlayer p) {
		return groupManager.getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroup(p.getName());
	}
	public static String[] GroupManager_getGroups(ITabPlayer p) {
		return groupManager.getWorldsHolder().getWorldPermissions(p.getWorldName()).getGroups(p.getName());
	}
	public static boolean iDisguise_isDisguised(ITabPlayer p) {
		return idisguise.isDisguised(((TabPlayer)p).player);
	}
	public static boolean LibsDisguises_isDisguised(ITabPlayer p) {
		return me.libraryaddict.disguise.DisguiseAPI.isDisguised(((TabPlayer)p).player);
	}
	public static String[] LuckPerms_getAllGroups(ITabPlayer p) {
		List<String> groups = new ArrayList<String>();
		for (LocalizedNode node : LuckPerms.getApi().getUser(p.getUniqueId()).getAllNodes()) if (node.isGroupNode()) groups.add(node.getGroupName());
		return groups.toArray(new String[0]);
	}
	public static String LuckPerms_getPrimaryGroup(ITabPlayer p) {
		return LuckPerms.getApi().getUser(p.getUniqueId()).getPrimaryGroup();
	}
	@SuppressWarnings("deprecation")
	public static String[] PermissionsEx_getGroupNames(ITabPlayer p) {
		return PermissionsEx.getUser(((TabPlayer)p).player).getGroupNames();
	}
	public static String PlaceholderAPI_setPlaceholders(ITabPlayer p, String s) {
		try {
			if (placeholderAPI) return PlaceholderAPI.setPlaceholders(((TabPlayer)p).player, s);
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.error(null, "PlaceholderAPI replace task failed. PlaceholderAPI version: " + papi.getDescription().getVersion());
				Shared.error(null, "String to replace: " + s);
				Shared.error(null, "Please send this error to the FIRST author whose name or plugin name you see here:", t);
			} else {
				//thats why it failed
				placeholderAPI = false;
			}
		}
		return s;
	}
	public static String PlaceholderAPI_setRelationalPlaceholders(ITabPlayer one, ITabPlayer two, String s) {
		try {
			if (placeholderAPI) return PlaceholderAPI.setRelationalPlaceholders(((TabPlayer)one).player, ((TabPlayer)two).player, s);
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
			return Shared.error(ProtocolVersion.SERVER_VERSION.getNetworkId(), "An error occured when getting protocol version of " + p.getName() + " using ProtocolSupport", e);
		}
	}
	public static int ViaVersion_getPlayerVersion(ITabPlayer p){
		try {
			return Via.getAPI().getPlayerVersion(p.getUniqueId());
		} catch (Throwable e) {
			return Shared.error(ProtocolVersion.SERVER_VERSION.getNetworkId(), "An error occured when getting protocol version of " + p.getName() + " using ViaVersion", e);
		}
	}
	public static boolean xAntiAFK_isAfk(ITabPlayer p) {
		return xAntiAFKAPI.isAfk(((TabPlayer)p).player);
	}
}
