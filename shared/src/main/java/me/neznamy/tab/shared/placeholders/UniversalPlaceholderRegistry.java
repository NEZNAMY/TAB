package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");
	
	//placeholder buffer
	private List<Placeholder> placeholders;

	@Override
	public List<Placeholder> registerPlaceholders() {
		placeholders = new ArrayList<>();
		Map<String, Object> serverAliases = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("server-aliases");
		placeholders.add(new PlayerPlaceholder("%"+TAB.getInstance().getPlatform().getSeparatorType()+"%", 1000) {
			public String get(TabPlayer p) {
				if (serverAliases.containsKey(p.getWorldName())) return serverAliases.get(p.getWorldName()).toString(); //bungee only
				return p.getWorldName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%"+TAB.getInstance().getPlatform().getSeparatorType()+"online%", 1000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) count++;
				}
				return String.valueOf(count);
			}
		});

		placeholders.add(new PlayerPlaceholder("%nick%", 100000000) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%player%", 100000000) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		double timeOffset = TAB.getInstance().getConfiguration().getConfig().getDouble("placeholders.time-offset", 0);
		SimpleDateFormat timeFormat = TAB.getInstance().getErrorManager().createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		placeholders.add(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return timeFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000)));
			}
		});
		SimpleDateFormat dateFormat = TAB.getInstance().getErrorManager().createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		placeholders.add(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return dateFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000)));
			}
		});
		placeholders.add(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return String.valueOf(TAB.getInstance().getPlayers().size());
			}
		});
		placeholders.add(new PlayerPlaceholder("%ping%", 50) {
			public String get(TabPlayer p) {
				return String.valueOf(p.getPing());
			}
		});
		placeholders.add(new PlayerPlaceholder("%player-version%", 100000000) {
			public String get(TabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%player-version-id%", 100000000) {
			public String get(TabPlayer p) {
				return String.valueOf(p.getVersion().getNetworkId());
			}
		});
		placeholders.add(new ServerPlaceholder("%maxplayers%", 100000000) {
			public String get() {
				return String.valueOf(TAB.getInstance().getPlatform().getMaxPlayers());
			}
		});
		placeholders.add(new ServerPlaceholder("%%", 100000000) {
			public String get() {
				return "%";
			}
		});
		registerLuckPermsPlaceholders();
		registerMemoryPlaceholders();
		registerStaffPlaceholders();
		registerRankPlaceholder();
		registerAnimationPlaceholders();
		if (TAB.getInstance().isPremium()) registerConditionPlaceholders();
		return placeholders;
	}

	/**
	 * Registers all memory-related placeholders
	 */
	private void registerMemoryPlaceholders() {
		placeholders.add(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return String.valueOf(((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)));
			}
		});
		placeholders.add(new ServerPlaceholder("%memory-max%", 100000000) {
			public String get() {
				return String.valueOf(((int) (Runtime.getRuntime().maxMemory() / 1048576)));
			}
		});
		placeholders.add(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return String.valueOf(decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
			}
		});
		placeholders.add(new ServerPlaceholder("%memory-max-gb%", 100000000) {
			public String get() {
				return String.valueOf(decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024));
			}
		});
	}

	/**
	 * Registers luckperms placeholders
	 */
	private void registerLuckPermsPlaceholders() {
		if (TAB.getInstance().getPermissionPlugin() instanceof LuckPerms) {
			placeholders.add(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)TAB.getInstance().getPermissionPlugin()).getPrefix(p);
				}
			});
			placeholders.add(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)TAB.getInstance().getPermissionPlugin()).getSuffix(p);
				}
			});
		}
	}

	/**
	 * Registers staff placeholders
	 */
	private void registerStaffPlaceholders() {
		placeholders.add(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.isStaff()) count++;
				}
				return String.valueOf(count);
			}
		});
		placeholders.add(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int count = TAB.getInstance().getPlayers().size();
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.isStaff()) count--;
				}
				return String.valueOf(count);
			}
		});
	}

	/**
	 * Registers %rank% placeholder
	 */
	private void registerRankPlaceholder() {
		Map<Object, Object> rankAliases = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("rank-aliases");
		placeholders.add(new PlayerPlaceholder("%rank%", 1000) {
			public String get(TabPlayer p) {
				for (Entry<Object, Object> entry : rankAliases.entrySet()) {
					if (String.valueOf(entry.getKey()).equalsIgnoreCase(p.getGroup())) {
						return entry.getValue().toString();
					}
				}
				if (rankAliases.containsKey("_OTHER_")) {
					return rankAliases.get("_OTHER_").toString();
				}
				return p.getGroup();
			}

			@Override
			public String[] getNestedStrings(){
				Set<String> list = new HashSet<>();
				for (Object value : rankAliases.values()) {
					list.add(String.valueOf(value).replace("%rank%", "")); //preventing stack overflow when someone uses %rank% as value
				}
				return list.toArray(new String[0]);
			}
		});
	}

	/**
	 * Registers animations
	 */
	private void registerAnimationPlaceholders() {
		for (Object s : TAB.getInstance().getConfiguration().getAnimationFile().getValues().keySet()) {
			Animation a = new Animation(s.toString(), TAB.getInstance().getConfiguration().getAnimationFile().getStringList(s + ".texts"), TAB.getInstance().getConfiguration().getAnimationFile().getInt(s + ".change-interval", 0));
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

	/**
	 * Registers conditions
	 */
	private void registerConditionPlaceholders() {
		Condition.setConditions(new HashMap<>());
		for (Object condition : TAB.getInstance().getConfiguration().getPremiumConfig().getConfigurationSection("conditions").keySet()) {
			List<String> list = TAB.getInstance().getConfiguration().getPremiumConfig().getStringList("conditions." + condition + ".conditions"); //lol
			String type = TAB.getInstance().getConfiguration().getPremiumConfig().getString("conditions." + condition + ".type");
			String yes = TAB.getInstance().getConfiguration().getPremiumConfig().getString("conditions." + condition + ".true");
			String no = TAB.getInstance().getConfiguration().getPremiumConfig().getString("conditions." + condition + ".false");
			Condition c = Condition.compile(condition.toString(), list, type, yes, no);
			Condition.getConditions().put(condition.toString(), c);
			String identifier = "%condition:" + c.getName() + "%";
			TAB.getInstance().getPlaceholderManager().getAllUsedPlaceholderIdentifiers().add(identifier);
			int refresh = TAB.getInstance().getConfiguration().getConfig().getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
			if (TAB.getInstance().getPlaceholderManager().getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
				refresh = TAB.getInstance().getPlaceholderManager().getPlayerPlaceholderRefreshIntervals().get(identifier);
			}
			placeholders.add(new PlayerPlaceholder(identifier, refresh) {

				@Override
				public String get(TabPlayer p) {
					return c.getText(p);
				}

				@Override
				public String[] getNestedStrings(){
					List<String> list = new ArrayList<>(Arrays.asList(super.getNestedStrings()));
					list.addAll(Arrays.asList(c.getYes(), c.getNo()));
					return list.toArray(new String[0]);
				}

			});
		}
	}
}