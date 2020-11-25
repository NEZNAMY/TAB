package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.conditions.Condition;
import me.neznamy.tab.shared.Animation;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.permission.LuckPerms;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");

	@Override
	public void registerPlaceholders() {
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"%", 1000) {
			public String get(TabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"online%", 1000) {
			public String get(TabPlayer p) {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});

		Placeholders.registerPlaceholder(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%player%", 999999999) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return Shared.getPlayers().size()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%ping%", 500) {
			public String get(TabPlayer p) {
				return p.getPing()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%player-version%", 999999999) {
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
	}

	private void registerMemoryPlaceholders() {
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-max%", -1) {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-max-gb%", -1) {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
	}

	private void registerLuckPermsPlaceholders() {
		if (Shared.permissionPlugin instanceof LuckPerms) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)Shared.permissionPlugin).getPrefix(p);
				}
			});
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)Shared.permissionPlugin).getSuffix(p);
				}
			});
		}
	}

	private void registerStaffPlaceholders() {
		Placeholders.registerPlaceholder(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%nonstaffonline%", 2000) {
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
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%rank%", 1000) {
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

	//making it this complicated to fix case-sensitivity
	private void registerAnimationPlaceholders() {
		main:
			for (String identifier : Placeholders.allUsedPlaceholderIdentifiers) {
				if (identifier.startsWith("%animation:")) {
					String animationName = identifier.substring(11, identifier.length()-1);
					for (Animation a : Configs.animations) {
						if (a.getName().equalsIgnoreCase(animationName)) {
							Placeholders.registerPlaceholder(new ServerPlaceholder(identifier, 50) {

								public String get() {
									return a.getMessage();
								}

								@Override
								public String[] getNestedStrings(){
									return a.getAllMessages();
								}

							});
							continue main;
						}
					}
					Shared.errorManager.startupWarn("Unknown animation &e\"" + animationName + "\"&c used in configuration. You need to define it in animations.yml");
				}
			}
	}

	//making it this complicated to fix case-sensitivity
	private void registerConditionPlaceholders() {
		main:
		for (String identifier : Placeholders.allUsedPlaceholderIdentifiers) {
			if (identifier.startsWith("%condition:")) {
				String conditionName = identifier.substring(11, identifier.length()-1);
				for (Condition c : Premium.conditions.values()) {
					if (c.getName().equalsIgnoreCase(conditionName)) {
						Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, PlaceholderManager.getInstance().defaultRefresh) {

							@Override
							public String get(TabPlayer p) {
								return c.getText(p);
							}

							@Override
							public String[] getNestedStrings(){
								return new String[] {c.yes, c.no};
							}

						});
						continue main;
					}
				}
				Shared.errorManager.startupWarn("Unknown condition &e\"" + conditionName + "\"&c used in configuration. You need to define it in premiumconfig.yml");
			}
		}
	}
}