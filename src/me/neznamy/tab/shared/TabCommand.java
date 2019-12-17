package me.neznamy.tab.shared;

import java.util.*;
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
					sendMessage(sender, "&6Attempting to parse string &e" + args[1] + "&6 for player &e" + sender.getName());
					sendMessage(sender, "&6Result: &r" + replaced + " &r(" + replaced.replace("&", "&") + ")");
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
					int dataSize = Shared.cpuHistory.size();
					sendMessage(sender, " ");
					sendMessage(sender, "&8&l&m╔             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
					sendMessage(sender, "&8&l║ &6TAB CPU STATS FROM THE LAST MINUTE");
					sendMessage(sender, "&8&l&m╠                                       ");
					sendMessage(sender, "&8&l║ &6Placeholders using over 0.01%:");
					Map<String, Long> placeholders = sortByValue(getPlaceholderCpu(dataSize));
					for (Entry<String, Long> entry : placeholders.entrySet()) {
						if (entry.getValue()/dataSize > 100000) sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - &a" + colorizePlaceholder(Shared.decimal3.format((float)entry.getValue()/dataSize/10000000)) + "%");
					}
					long placeholdersTotal = 0;
					for (Long time : placeholders.values()) placeholdersTotal += time;
					sendMessage(sender, "&8&l&m╠                                       ");
					sendMessage(sender, "&8&l║ &6Feature specific:");
					Map<Feature, Long> features = sortByValue(getFeatureCpu(dataSize));
					for (Entry<Feature, Long> entry : features.entrySet()) {
						sendMessage(sender, "&8&l║ &7" + entry.getKey().toString() + " - &a" + colorizeFeature(Shared.decimal3.format((float)entry.getValue()/dataSize/10000000)) + "%");
					}
					long featuresTotal = 0;
					for (Long time : features.values()) featuresTotal += time;
					sendMessage(sender, "&8&l&m╠                                       ");
					sendMessage(sender, "&8&l║ &7&lPLACEHOLDERS TOTAL: &a&l" + Shared.decimal3.format((float)placeholdersTotal/dataSize/10000000) + "%");
					sendMessage(sender, "&8&l║ &7&lPLUGIN TOTAL: &e&l" + Shared.decimal3.format((float)featuresTotal/dataSize/10000000) + "%");
					sendMessage(sender, "&8&l&m╚             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
					sendMessage(sender, " ");	
				} else sendMessage(sender, Configs.no_perm);
			} else help(sender);
		} else help(sender);
	}
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());
		Map<K, V> result = new LinkedHashMap<>();
		for (int i=list.size()-1; i>=0; i--) {
			Entry<K, V> entry = list.get(i);
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	private static HashMap<Feature, Long> getFeatureCpu(int history) {
		HashMap<Feature, Long> values = new HashMap<Feature, Long>();
		for (int i=0; i<Shared.cpuHistory.size(); i++) {
			for (Entry<Feature, Long> entry : Shared.cpuHistory.get(i).getValues().entrySet()) {
				Feature feature = entry.getKey();
				if (!values.containsKey(feature)) values.put(feature, 0L);
				values.put(feature, values.get(feature)+entry.getValue());
			}
		}
		return values;
	}
	private static HashMap<String, Long> getPlaceholderCpu(int history) {
		HashMap<String, Long> values = new HashMap<String, Long>();
		for (ConcurrentHashMap<String, Long> sample : Shared.placeholderCpuHistory) {
			for (Entry<String, Long> placeholder : sample.entrySet()) {
				if (!values.containsKey(placeholder.getKey())) values.put(placeholder.getKey(), 0L);
				values.put(placeholder.getKey(), values.get(placeholder.getKey()) + placeholder.getValue());
			}
		}
		return values;
	}
	private static String colorizePlaceholder(String value) {
		float f = Float.parseFloat(value.replace(",", "."));
		if (f > 1) return "&c" + value;
		if (f > 0.3) return "&e" + value;
		return "&a" + value;
	}
	private static String colorizeFeature(String value) {
		float f = Float.parseFloat(value.replace(",", "."));
		if (f > 5) return "&c" + value;
		if (f > 1) return "&e" + value;
		return "&a" + value;
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
		sendMessage(sender, "&3[TAB] &a&lShowing debug information");
		sendMessage(sender, "&7&m>-------------------------------<");
		sendMessage(sender, "&6PlaceholderAPI: &a" + PluginHooks.placeholderAPI);
		sendMessage(sender, "&6Found Permission system: &a" + Shared.mainClass.getPermissionPlugin());
		if (Configs.usePrimaryGroup) {
			sendMessage(sender, "&6Permission group choice logic: &aPrimary group&8/&r&8&mChoose from list");
		} else {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&r&8/&aChoose from list");
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
						sortingType = "Permissions &c(this option was enabled by user, it is disabled by default!)";
					} else {
						sortingType = "Groups";
					}
				}
			}
		} else {
			sortingType = "&cDISABLED";
		}
		sendMessage(sender, "&6Sorting system: &a" + sortingType);
		sendMessage(sender, "&7&m>-------------------------------<");
		if (analyzed != null) {
			sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
			if (Configs.usePrimaryGroup) {
				sendMessage(sender, "&ePrimary permission group: &a" + analyzed.getGroup());
			} else {
				sendMessage(sender, "&eFull permission group list: &a" + Arrays.toString(analyzed.getGroupsFromPermPlugin()));
				sendMessage(sender, "&eChosen group: &a" + analyzed.getGroup());
			}
			if (sorting) {
				if (analyzed.disabledNametag) {
					sendMessage(sender, "&eTeam name: &cSorting disabled in player's world");
				} else {
					sendMessage(sender, "&eTeam name: &a" + analyzed.getTeamName());
				}
			}
			if (Playerlist.enable) {
				if (analyzed.disabledTablistNames) {
					sendMessage(sender, "&9tabprefix: &cDisabled in player's world");
					sendMessage(sender, "&9tabsuffix: &cDisabled in player's world");
					sendMessage(sender, "&9tabname: &cDisabled in player's world");
				} else {
					sendMessage(sender, "&9tabprefix: &b" + analyzed.properties.get("tabprefix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tabprefix").isStatic() + ")");
					sendMessage(sender, "&9tabsuffix: &b" + analyzed.properties.get("tabsuffix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tabsuffix").isStatic() + ")");
					sendMessage(sender, "&9tabname: &b" + analyzed.properties.get("customtabname").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("customtabname").isStatic() + ")");
				}
			}
			if (NameTag16.enable || Configs.unlimitedTags) {
				if (analyzed.disabledNametag) {
					sendMessage(sender, "&9tagprefix: &cDisabled in player's world");
					sendMessage(sender, "&9tagsuffix: &cDisabled in player's world");
				} else {
					sendMessage(sender, "&9tagprefix: &b" + analyzed.properties.get("tagprefix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tagprefix").isStatic() + ")");
					sendMessage(sender, "&9tagsuffix: &b" + analyzed.properties.get("tagsuffix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tagsuffix").isStatic() + ")");
				}
			}
			if (Configs.unlimitedTags) {
				if (analyzed.disabledNametag) {
					sendMessage(sender, "&9abovename: &cDisabled in player's world");
					sendMessage(sender, "&9belowname: &cDisabled in player's world");
					sendMessage(sender, "&9tagname: &cDisabled in player's world");
				} else {
					sendMessage(sender, "&9abovename: &b" + analyzed.properties.get("abovename").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("abovename").isStatic() + ")");
					sendMessage(sender, "&9belowname: &b" + analyzed.properties.get("belowname").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("belowname").isStatic() + ")");
					sendMessage(sender, "&9tagname: &b" + analyzed.properties.get("customtagname").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("customtagname").isStatic() + ")");
				}
			}
		}
	}
	public static void help(ITabPlayer sender){
		if (sender == null) Shared.mainClass.sendConsoleMessage("&3TAB v" + Shared.pluginVersion);
		if (isAdmin(sender) && !Shared.disabled) {
			for (String msg : Configs.help_menu) sendMessage(sender, msg);
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
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player").replace("&", "&"));
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
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", group).replace("%category%", "group").replace("&", "&"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", group).replace("%category%", "group"));
		}
	}
}