package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class TabCommand{

	private static final String[] usualProperties = {"tabprefix", "tabsuffix", "tagprefix", "tagsuffix", "customtabname"};
	private static final String[] extraProperties = {"abovename", "belowname", "customtagname"};

	public static void execute(ITabPlayer sender, String[] args){
		if (Shared.disabled && isAdmin(sender)) {
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				Shared.mainClass.reload(sender);
			} else {
				sendMessage(sender, Configs.plugin_disabled);
			}
			return;
		}
		if (args.length == 4 && args[0].equalsIgnoreCase("announce")) {
			//tab announce bar <name> <length>
			String type = args[1];
			if (type.equalsIgnoreCase("bar")) {
				if (can(sender, "announce.bar")) {
					String barname = args[2];
					int duration;
					try {
						duration = Integer.parseInt(args[3]);
						int d2 = duration;
						Shared.exe.submit(new Runnable() {

							public void run() {
								try {
									BossBarLine bar = BossBar.getLine(barname);
									if (bar == null) {
										sender.sendMessage("Bar not found");
										return;
									}
									BossBar.announcements.add(barname);
									for (ITabPlayer all : Shared.getPlayers()) {
										PacketAPI.createBossBar(all, bar);
									}
//									List<String> animationFrames = //maybe later
									for (int i=0; i<(float)d2*1000/BossBar.refresh; i++) {
										Thread.sleep(BossBar.refresh);
									}
									for (ITabPlayer all : Shared.getPlayers()) {
										PacketAPI.removeBossBar(all, bar);
									}
									BossBar.announcements.remove(barname);
								} catch (Exception e) {

								}
							}
						});
					} catch (Exception e) {
						sender.sendMessage(args[3] + " is not a number!");
					}
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			}
		} else if (args.length >= 3){
			//tab object <name> type [value]
			//tab object <name> remove
			String type = args[2].toLowerCase();
			String value = "";
			for (int i=3; i<args.length; i++){
				if (i>3) value += " ";
				value += args[i];
			}
			for (String property : usualProperties) {
				if (type.equals(property)) {
					if (canChangeProperty(sender, property)) {
						save(sender, args[0], args[1], type, value);
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
			for (String property : extraProperties) {
				if (type.equals(property)) {
					if (canChangeProperty(sender, property)) {
						save(sender, args[0], args[1], type, value);
						if (!Configs.unlimitedTags) {
							sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
						}
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
			if (type.equals("remove")) {
				if (can(sender, "remove")) {
					if (args[0].equals("group")) {
						Configs.config.set("Groups." + args[1], null);
						Configs.config.save();
					} else if (args[0].equals("player")) {
						Configs.config.set("Users." + args[1], null);
						Configs.config.save();
						ITabPlayer pl = Shared.getPlayer(args[1]);
						if (pl != null) {
							pl.updateAll();
							if (Configs.unlimitedTags) pl.restartArmorStands();
						}
					} else help(sender);
					sendMessage(sender, Configs.data_removed.replace("%category%", args[0]).replace("%value%", args[1]));
				}
				return;
			}
			help(sender);
		} else if (args.length == 2) {
			//tab debug <player>
			//tab parse <string>
			if (args[0].equalsIgnoreCase("debug")) {
				if (can(sender, "debug")){
					ITabPlayer analyzed = Shared.getPlayer(args[1]);
					if (analyzed != null) debug(sender, analyzed);
					else sendMessage(sender, Configs.player_not_found);
				} else sendMessage(sender, Configs.no_perm);
			} else if (args[0].equalsIgnoreCase("parse")) {
				if (sender != null) {
					String replaced = Placeholders.replaceAllPlaceholders(args[1], sender);
					sendMessage(sender, "§6Attempting to parse string §e" + args[1] + "§6 for player §e" + sender.getName());
					sendMessage(sender, "§6Result: §r" + replaced + " §r(" + replaced.replace("§", "&") + ")");
				}
			}
		} else if (args.length == 1){
			//tab reload
			//tab debug
			//tab cpu
			//tab ntpreview
			if (args[0].equalsIgnoreCase("reload")){
				if (can(sender, "reload")){
					Shared.mainClass.reload(sender);
				} else sendMessage(sender, Configs.no_perm);
			} else if (args[0].equalsIgnoreCase("debug")){
				if (can(sender, "debug")){
					debug(sender, null);
				} else sendMessage(sender, Configs.no_perm);
			} else if (args[0].equalsIgnoreCase("ntpreview")){
				if (Configs.unlimitedTags) {
					if (sender != null) {
						if (sender.previewingNametag) {
							NameTagLineManager.destroy(sender, sender);
							sendMessage(sender, Configs.preview_off);
						} else {
							NameTagLineManager.spawnArmorStand(sender, sender, false);
							sendMessage(sender, Configs.preview_on);
						}
						sender.previewingNametag = !sender.previewingNametag;
					}
				} else sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
			} else if (args[0].equalsIgnoreCase("cpu")){
				if (can(sender, "cpu")) {
					sendMessage(sender, "§3[TAB] §a--------------------------------------");
					sendMessage(sender, "§3[TAB] §6CPU from last 1m, 5m, 15m: §a" + getTotalCpu(60) + ", " + getTotalCpu(300) + ", " + getTotalCpu(900));
					sendMessage(sender, "§3[TAB] §a--------------------------------------");
					int minute = 60;
					if (Shared.cpuHistory.size() >= minute) {
						HashMap<Feature, Long> lastMinute = new HashMap<Feature, Long>();
						for (int i=Shared.cpuHistory.size()-minute; i<Shared.cpuHistory.size(); i++) {
							for (Entry<Feature, Long> entry : Shared.cpuHistory.get(i).getValues().entrySet()) {
								Feature feature = entry.getKey();
								if (!lastMinute.containsKey(feature)) lastMinute.put(feature, 0L);
								lastMinute.put(feature, lastMinute.get(feature)+entry.getValue());
							}
						}
						sendMessage(sender, "§3[TAB] §aFeature specific from the last minute:");
						for (Entry<Feature, Long> entry : lastMinute.entrySet()) {
							sendMessage(sender, "§3[TAB] §6" + entry.getKey().toString() + " - §a" + Shared.decimal3.format((float)entry.getValue()/minute/10000000) + "%");
						}
						sendMessage(sender, "§3[TAB] §a--------------------------------------");
						sendMessage(sender, "§3[TAB] §aPlaceholders from the last minute using over 0.01%:");
						HashMap<String, Long> total = new HashMap<String, Long>();
						for (ConcurrentHashMap<String, Long> sample : Shared.placeholderCpuHistory) {
							for (Entry<String, Long> placeholder : sample.entrySet()) {
								if (!total.containsKey(placeholder.getKey())) total.put(placeholder.getKey(), 0L);
								total.put(placeholder.getKey(), total.get(placeholder.getKey()) + placeholder.getValue());
							}
						}
						for (Entry<String, Long> entry : total.entrySet()) {
							if (entry.getValue()/60 > 100000) sendMessage(sender, "§3[TAB] §6" + entry.getKey() + " - §a" + Shared.decimal3.format((float)entry.getValue()/minute/10000000) + "%");
						}
						long placeholdersTotal = 0;
						for (Long time : total.values()) {
							placeholdersTotal += time;
						}
						sendMessage(sender, "§3[TAB] §e§lPLACEHOLDERS TOTAL: §2§l" + Shared.decimal3.format((float)placeholdersTotal/minute/10000000) + "%");
						sendMessage(sender, "§3[TAB] §a--------------------------------------");
					}
				} else sendMessage(sender, Configs.no_perm);
			} else help(sender);
		} else help(sender);
	}
	private static String getTotalCpu(int history) {
		String cpu = "-";
		if (Shared.cpuHistory.size() >= history) {
			long var = 0;
			for (int i=Shared.cpuHistory.size()-history; i<Shared.cpuHistory.size(); i++) {
				var += Shared.cpuHistory.get(i).getTotalCpuTime();
			}
			cpu = Shared.decimal3.format((float)var/history/10000000) + "%";
		}
		return cpu;
	}
	public static void save(ITabPlayer sender, String arg0, String arg1, String type, String value) {
		if (arg0.equalsIgnoreCase("group")){
			saveGroup(sender, arg1, type, value);
		} else if (arg0.equalsIgnoreCase("player")){
			savePlayer(sender, arg1, type, value);
		} else help(sender);
	}
	public static boolean canChangeProperty(ITabPlayer sender, String property) {
		return can(sender, "change." + property);
	}
	public static boolean canChange(ITabPlayer sender) {
		if (sender == null || sender.hasPermission("tab.change.*")) return true;
		return isAdmin(sender);
	}
	public static boolean can(ITabPlayer sender, String action) {
		if (sender == null || sender.hasPermission("tab." + action)) return true;
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
			analyzed = Shared.getPlayer(sender.getUniqueId());
		}
		sendMessage(sender, "§3[TAB] §a§lShowing debug information");
		sendMessage(sender, "§7§m>-------------------------------<");
		sendMessage(sender, "§6PlaceholderAPI: §a" + PluginHooks.placeholderAPI);
		sendMessage(sender, "§6Found Permission system: §a" + Shared.mainClass.getPermissionPlugin());
		if (Configs.usePrimaryGroup) {
			sendMessage(sender, "§6Permission group choice logic: §aPrimary group§8/§r§8§mChoose from list");
		} else {
			sendMessage(sender, "§6Permission group choice logic: §8§mPrimary group§r§8/§aChoose from list");
		}
		boolean sorting = Configs.unlimitedTags || NameTag16.enable;
		String sortingType;

		if (sorting) {
			if (Premium.is()) {
				sortingType = Premium.sortingType.toString();
				if (sortingType.contains("PLACEHOLDER")) sortingType += " - " + Premium.sortingPlaceholder;
			} else {
				if (Configs.sortedGroups.isEmpty()) {
					sortingType = "Tabprefix";
				} else {
					if (Configs.sortByPermissions) {
						sortingType = "Permissions (ENABLED BY USER, DISABLED BY DEFAULT!)";
					} else {
						sortingType = "Groups";
					}
				}
			}
		} else {
			sortingType = "§cDISABLED";
		}
		sendMessage(sender, "§6Sorting system: §a" + sortingType);
		sendMessage(sender, "§7§m>-------------------------------<");
		if (analyzed != null) {
			sendMessage(sender, "§ePlayer: §a" + analyzed.getName());
			if (Configs.usePrimaryGroup) {
				sendMessage(sender, "§ePrimary permission group: §a" + analyzed.getGroup());
			} else {
				sendMessage(sender, "§eFull permission group list: §a" + Arrays.toString(analyzed.getGroupsFromPermPlugin()));
				sendMessage(sender, "§eChosen group: §a" + analyzed.getGroup());
			}
			if (sorting) sendMessage(sender, "§eTeam name: §a" +analyzed.getTeamName().replace("§", "&"));
			if (Playerlist.enable) {
				sendMessage(sender, "§9tabprefix: §b" + analyzed.properties.get("tabprefix").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("tabprefix").isStatic() + ")");
				sendMessage(sender, "§9tabsuffix: §b" + analyzed.properties.get("tabsuffix").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("tabsuffix").isStatic() + ")");
				sendMessage(sender, "§9tabname: §b" + analyzed.properties.get("customtabname").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("customtabname").isStatic() + ")");
			}
			if (NameTag16.enable || Configs.unlimitedTags) {
				sendMessage(sender, "§9tagprefix: §b" + analyzed.properties.get("tagprefix").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("tagprefix").isStatic() + ")");
				sendMessage(sender, "§9tagsuffix: §b" + analyzed.properties.get("tagsuffix").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("tagsuffix").isStatic() + ")");
			}
			if (Configs.unlimitedTags) {
				sendMessage(sender, "§9abovename: §b" + analyzed.properties.get("abovename").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("abovename").isStatic() + ")");
				sendMessage(sender, "§9belowname: §b" + analyzed.properties.get("belowname").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("belowname").isStatic() + ")");
				sendMessage(sender, "§9tagname: §b" + analyzed.properties.get("customtagname").getCurrentRawValue() + " §7(static=" + analyzed.properties.get("customtagname").isStatic() + ")");
			}
		}
	}
	public static void help(ITabPlayer sender){
		if (sender == null) Shared.mainClass.sendConsoleMessage("§3TAB v" + Shared.pluginVersion);
		if (isAdmin(sender) && !Shared.disabled) {
			for (Object msg : Configs.help_menu) sendMessage(sender, (msg+"").replace("&", "§"));
		}
	}
	public static void savePlayer(ITabPlayer sender, String player, String type, String value){
		ITabPlayer pl = Shared.getPlayer(player);
		if (pl != null) {
			pl.setProperty(type, value);
			pl.forceUpdateDisplay();
		}
		if (value.length() == 0) value = null;
		Configs.config.set("Users." + player + "." + type, value);
		Configs.config.save();
		if (value != null){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player").replace("§", "§"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", player).replace("%category%", "player"));
		}
	}
	public static void saveGroup(ITabPlayer sender, String group, String type, String value){
		for (ITabPlayer pl : Shared.getPlayers()) {
			if (pl.getGroup() != null && pl.getGroup().equals(group)){
				pl.setProperty(type, value);
				pl.forceUpdateDisplay();
			}
		}
		if (value.length() == 0) value = null;
		Configs.config.set("Groups." + group + "." + type, value);
		Configs.config.save();
		if (value != null){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", group).replace("%category%", "group").replace("§", "§"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", group).replace("%category%", "group"));
		}
	}
}