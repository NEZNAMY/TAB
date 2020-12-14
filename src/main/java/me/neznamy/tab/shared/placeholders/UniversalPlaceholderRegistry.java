package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");
	
	private static List<Placeholder> placeholders;

	@Override
	public List<Placeholder> registerPlaceholders() {
		placeholders = new ArrayList<Placeholder>();
		placeholders.add(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"%", 1000) {
			public String get(TabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"online%", 1000) {
			public String get(TabPlayer p) {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});

		placeholders.add(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%player%", 999999999) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		placeholders.add(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		placeholders.add(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		placeholders.add(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return Shared.getPlayers().size()+"";
			}
		});
		placeholders.add(new PlayerPlaceholder("%ping%", 500) {
			public String get(TabPlayer p) {
				return p.getPing()+"";
			}
		});
		placeholders.add(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(TabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		registerLuckPermsPlaceholders();
		registerMemoryPlaceholders();
		registerStaffPlaceholders();
		registerRankPlaceholder();
		registerAnimationPlaceholders();
		registerConditionPlaceholders();
		return placeholders;
	}

	private void registerMemoryPlaceholders() {
		placeholders.add(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		placeholders.add(new ServerPlaceholder("%memory-max%", -1) {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		placeholders.add(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		placeholders.add(new ServerPlaceholder("%memory-max-gb%", -1) {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
	}

	private void registerLuckPermsPlaceholders() {
		if (Shared.permissionPlugin instanceof LuckPerms) {
			placeholders.add(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)Shared.permissionPlugin).getPrefix(p);
				}
			});
			placeholders.add(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)Shared.permissionPlugin).getSuffix(p);
				}
			});
		}
	}

	private void registerStaffPlaceholders() {
		placeholders.add(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		placeholders.add(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = Shared.getPlayers().size();
				for (TabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
	}

	private void registerRankPlaceholder() {
		placeholders.add(new PlayerPlaceholder("%rank%", 1000) {
			public String get(TabPlayer p) {
				for (Entry<Object, Object> entry : Configs.rankAliases.entrySet()) {
					if (String.valueOf(entry.getKey()).equalsIgnoreCase(p.getGroup())) {
						return entry.getValue().toString();
					}
				}
				if (Configs.rankAliases.containsKey("_OTHER_")) {
					return Configs.rankAliases.get("_OTHER_").toString();
				}
				return p.getGroup();
			}

			@Override
			public String[] getNestedStrings(){
				Set<String> list = new HashSet<String>();
				for (Object value : Configs.rankAliases.values()) {
					list.add(String.valueOf(value).replace("%rank%", "")); //preventing stack overflow when someone uses %rank% as value
				}
				return list.toArray(new String[0]);
			}
		});
	}

	private void registerAnimationPlaceholders() {
		for (Animation a : Configs.animations) {
			placeholders.add(new ServerPlaceholder("%animation:" + a.getName() + "%", 50) {

				public String get() {
					return a.getMessage();
				}

				@Override
				public String[] getNestedStrings(){
					return a.getAllMessages();
				}

			});
		}
	}

	//making it this complicated to fix case-sensitivity
	private void registerConditionPlaceholders() {
		for (Condition c : Premium.conditions.values()) {
			placeholders.add(new PlayerPlaceholder("%condition:" + c.getName() + "%", ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).defaultRefresh) {

				@Override
				public String get(TabPlayer p) {
					return c.getText(p);
				}

				@Override
				public String[] getNestedStrings(){
					List<String> list = new ArrayList<String>(Arrays.asList(super.getNestedStrings()));
					list.addAll(Arrays.asList(new String[] {c.yes, c.no}));
					return list.toArray(new String[0]);
				}

			});
		}
	}
}