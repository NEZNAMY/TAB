package me.neznamy.tab.shared;

import java.util.Arrays;

import me.neznamy.tab.bukkit.NameTagLineManager;
import me.neznamy.tab.bukkit.packets.NMSClass;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.ServerType;

public class TabCommand{

	public static void execute(ITabPlayer sender, String[] args){
		if (Shared.mainClass.isDisabled() && isAdmin(sender)) {
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				Shared.mainClass.reload(sender);
			} else {
				sendMessage(sender, Configs.plugin_disabled);
			}
			return;
		}
		if (args.length >= 3){
			//tab object <name> type [value]
			//tab object <name> remove
			String type = args[2].toLowerCase();
			String value = "";
			for (int i=3; i<args.length; i++){
				if (i>3) value += " ";
				value += args[i];
			}
			if (type.equals("tabprefix")) {
				if (canChangeTabPrefix(sender)) {
					save(sender, args[0], args[1], type, value);
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("tabsuffix")) {
				if (canChangeTabSuffix(sender)) {
					save(sender, args[0], args[1], type, value);
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("tagprefix")) {
				if (canChangeTagPrefix(sender)) {
					if (value.length() > 16 && !value.contains("%") && !Configs.unlimitedTags && (NMSClass.versionNumber < 13 && NMSClass.versionNumber != 0)){
						sendMessage(sender, Configs.value_too_long.replace("%type%", type).replace("%length%", value.length()+""));
					} else {
						save(sender, args[0], args[1], type, value);
					}
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("tagsuffix")) {
				if (canChangeTagSuffix(sender)) {
					if (value.length() > 16 && !value.contains("%") && !Configs.unlimitedTags && (NMSClass.versionNumber < 13 && NMSClass.versionNumber != 0)){
						sendMessage(sender, Configs.value_too_long.replace("%type%", type).replace("%length%", value.length()+""));
					} else {
						save(sender, args[0], args[1], type, value);
					}
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("belowname")) {
				if (canChangeBelowName(sender)) {
					save(sender, args[0], args[1], type, value);
					if (!Configs.unlimitedTags) {
						sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
					}
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("abovename")) {
				if (canChangeAboveName(sender)) {
					save(sender, args[0], args[1], type, value);
					if (!Configs.unlimitedTags) {
						sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
					}
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("customtabname")) {
				if (canChangeCustomTabName(sender)) {
					save(sender, args[0], args[1], type, value);
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("customtagname")) {
				if (canChangeCustomTabName(sender)) {
					save(sender, args[0], args[1], type, value);
					if (!Configs.unlimitedTags) {
						sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
					}
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else if (type.equals("remove")) {
				if (canRemove(sender)) {
					if (args[0].equals("group")) {
						Configs.config.set("Groups." + args[1], null);
						Configs.config.save();
					} else if (args[0].equals("player")) {
						Configs.config.set("Users." + args[1], null);
						Configs.config.save();
						ITabPlayer pl = Shared.getPlayer(args[1]);
						if (pl != null) recalculatePlayer(pl);
					} else help(sender);
					sendMessage(sender, Configs.data_removed.replace("%category%", args[0]).replace("%value%", args[1]));
				}
			} else help(sender);
		} else if (args.length == 2) {
			//tab debug <player>
			//tab parse <string>
			if (args[0].equalsIgnoreCase("debug")) {
				if (canDebug(sender)){
					ITabPlayer analyzed = Shared.getPlayer(args[1]);
					if (analyzed != null) debug(sender, analyzed);
					else sendMessage(sender, Configs.player_not_found);
				} else sendMessage(sender, Configs.no_perm);
			} else if (args[0].equalsIgnoreCase("parse")) {
				if (sender != null) {
					sendMessage(sender, "§6Attempting to parse string §e" + args[1] + "§6 for player §e" + sender.getName());
					sendMessage(sender, "§6Result: §r" + Placeholders.replace(args[1], sender));
				}
			}
		} else if (args.length == 1){
			//tab reload
			//tab debug
			//tab cpu
			//tab ntpreview
			if (args[0].equalsIgnoreCase("reload")){
				if (canReload(sender)){
					Shared.mainClass.reload(sender);
				} else sendMessage(sender, Configs.no_perm);
			} else if (args[0].equalsIgnoreCase("debug")){
				if (canDebug(sender)){
					debug(sender, null);
				} else sendMessage(sender, Configs.no_perm);
			} else if (args[0].equalsIgnoreCase("ntpreview")){
				if (Configs.unlimitedTags) {
					if (sender != null) {
						NameTagLineManager.spawnArmorStand(sender, sender, false);
					}
				} else sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
			} else if (args[0].equalsIgnoreCase("cpu")){
				if (canCPU(sender)) {
					String Min1 = "-";
					if (Shared.cpuValues.size() >= 60) {
						long var = 0;
						for (int i=Shared.cpuValues.size()-60; i<Shared.cpuValues.size(); i++) {
							var += Shared.cpuValues.get(i);
						}
						Min1 = Shared.round((float)var/60/10000000) + "%";
					}
					String Min5 = "-";
					if (Shared.cpuValues.size() >= 300) {
						long var = 0;
						for (int i=Shared.cpuValues.size()-300; i<Shared.cpuValues.size(); i++) {
							var += Shared.cpuValues.get(i);
						}
						Min5 = Shared.round((float)var/300/10000000) + "%";
					}
					String Min15 = "-";
					if (Shared.cpuValues.size() >= 900) {
						long var = 0;
						for (int i=Shared.cpuValues.size()-900; i<Shared.cpuValues.size(); i++) {
							var += Shared.cpuValues.get(i);
						}
						Min15 = Shared.round((float)var/900/10000000) + "%";
					}
					sendMessage(sender, "§3[TAB] §6CPU from last 1m, 5m, 15m: §a" + Min1 + ", " + Min5 + ", " + Min15);
				} else sendMessage(sender, Configs.no_perm);
			} else help(sender);
		} else help(sender);
	}
	public static void save(ITabPlayer sender, String arg0, String arg1, String type, String value) {
		if (arg0.equalsIgnoreCase("group")){
			saveGroup(sender, arg1, type, value);
		} else if (arg0.equalsIgnoreCase("player")){
			savePlayer(sender, arg1, type, value);
		} else help(sender);
	}
	public static boolean canChangeTabPrefix(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.tabprefix")) return true;
		return canChange(sender);
	}
	public static boolean canChangeTabSuffix(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.tabsuffix")) return true;
		return canChange(sender);
	}
	public static boolean canChangeTagPrefix(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.tagprefix")) return true;
		return canChange(sender);
	}
	public static boolean canChangeTagSuffix(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.tagsuffix")) return true;
		return canChange(sender);
	}
	public static boolean canChangeBelowName(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.belowname")) return true;
		return canChange(sender);
	}
	public static boolean canChangeAboveName(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.abovename")) return true;
		return canChange(sender);
	}
	public static boolean canChangeCustomTabName(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.customtabname")) return true;
		return canChange(sender);
	}
	public static boolean canChangeCustomTagName(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.customtagname")) return true;
		return canChange(sender);
	}
	public static boolean canChange(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.*")) return true;
		return isAdmin(sender);
	}
	public static boolean canReload(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.reload")) return true;
		return isAdmin(sender);
	}
	public static boolean canDebug(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.debug")) return true;
		return isAdmin(sender);
	}
	public static boolean canCPU(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.cpu")) return true;
		return isAdmin(sender);
	}
	public static boolean canRemove(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.remove")) return true;
		return isAdmin(sender);
	}
	public static boolean isAdmin(ITabPlayer sender) {
		return sender == null || sender.hasPermission("tab.admin") || sender.hasPermission("tab.*");
	}
	public static void sendMessage(ITabPlayer sender, String message) {
		if (sender != null) {
			sender.sendMessage(message);
		} else {
			Shared.mainClass.sendConsoleMessage(message);
		}
	}
	public static void debug(ITabPlayer sender, ITabPlayer analyzed) {
		if (analyzed == null && sender != null) {
			analyzed = Shared.getPlayer(sender.getName());
		}
		sendMessage(sender, "§3[TAB] §a§lShowing debug information");
		sendMessage(sender, "§7§m>-------------------------------<");
		if (Shared.servertype == ServerType.BUKKIT) sendMessage(sender, "§6PlaceholderAPI: §a" + me.neznamy.tab.bukkit.Placeholders.placeholderAPI);
		sendMessage(sender, "§6Found Permission system: §a" + Shared.mainClass.getPermissionPlugin());
		if (Configs.usePrimaryGroup) {
			sendMessage(sender, "§ePermission group choice logic: §aPrimary group§8/§r§8§mChoose from list");
		} else {
			sendMessage(sender, "§ePermission group choice logic: §8§mPrimary group§r§8/§aChoose from list");
		}
		sendMessage(sender, "§7§m>-------------------------------<");
		if (analyzed != null) {
			sendMessage(sender, "§ePlayer: §a" + analyzed.getName());
			if (Configs.usePrimaryGroup) {
				sendMessage(sender, "§ePrimary permission group: §a" + analyzed.getGroupFromPermPlugin());
			} else {
				sendMessage(sender, "§eFull permission group list: §a" + Arrays.toString(analyzed.getGroupsFromPermPlugin()));
				sendMessage(sender, "§eChosen group: §a" + analyzed.getGroup());
			}
			boolean sorting = Configs.unlimitedTags || NameTag16.enable;
			sendMessage(sender, "§eTeam name: §a" + (!sorting? "§cSORTING IS DISABLED" : analyzed.getTeamName().replace("§", "&")));
			if (Shared.mainClass.listNames()) {
				sendMessage(sender, "§9tabprefix: §b" + analyzed.getTabPrefix());
				sendMessage(sender, "§9tabsuffix: §b" + analyzed.getTabSuffix());
				sendMessage(sender, "§9tabname: §b" + analyzed.getTabName());
			}
			if (NameTag16.enable || Configs.unlimitedTags) {
				sendMessage(sender, "§9tagprefix: §b" + analyzed.getTagPrefix());
				sendMessage(sender, "§9tagsuffix: §b" + analyzed.getTagSuffix());
			}
			if (Configs.unlimitedTags) {
				sendMessage(sender, "§9abovename: §b" + analyzed.getAboveName());
				sendMessage(sender, "§9belowname: §b" + analyzed.getBelowName());
				sendMessage(sender, "§9tagname: §b" + analyzed.getTagName());
			}
		}
	}
	public static void help(ITabPlayer sender){
		if (isAdmin(sender) && !Shared.mainClass.isDisabled()) {
			for (String msg : Configs.help_menu) sendMessage(sender, msg.replace("&", "§"));
		}
	}
	public static void savePlayer(ITabPlayer p, String player, String type, String value){
		if (value.equals("")) value = null;
		Configs.config.set("Users." + player + "." + type, value);
		Configs.config.save();
		ITabPlayer pl = Shared.getPlayer(player);
		if (pl != null) recalculatePlayer(pl);
		if (value != null){
			sendMessage(p, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player").replace("§", "§"));
		} else {
			sendMessage(p, Configs.value_removed.replace("%type%", type).replace("%unit%", player).replace("%category%", "player"));
		}
	}
	public static void saveGroup(ITabPlayer p, String group, String type, String value){
		if (value.equals("")) value = null;
		Configs.config.set("Groups." + group + "." + type, value);
		Configs.config.save();
		for (ITabPlayer pl : Shared.getPlayers()) {
			if (pl.getGroup() != null && pl.getGroup().equals(group)){
				recalculatePlayer(pl);
			}
		}
		if (value != null){
			sendMessage(p, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", group).replace("%category%", "group").replace("§", "§"));
		} else {
			sendMessage(p, Configs.value_removed.replace("%type%", type).replace("%unit%", group).replace("%category%", "group"));
		}
	}
	private static void recalculatePlayer(ITabPlayer pl) {
		pl.updateAll();
		if (NameTag16.enable || Configs.unlimitedTags) pl.updateTeam();
		if (Shared.mainClass.listNames()) pl.updatePlayerListName(true);
		if (Configs.unlimitedTags) pl.restartArmorStands();
	}
}