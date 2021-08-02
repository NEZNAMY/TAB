package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%world%", 1000) {
			public Object get(TabPlayer p) {
				return p.getWorld();
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%worldonline%", 1000) {
			public Object get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
					if (String.valueOf(p.getWorld()).equals(all.getWorld())) count++;
				}
				return count;
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%server%", 1000) {
			public Object get(TabPlayer p) {
				return p.getServer();
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%serveronline%", 1000) {
			public Object get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
					if (String.valueOf(p.getServer()).equals(all.getServer())) count++;
				}
				return count;
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%player%", 100000000) {
			public Object get(TabPlayer p) {
				return p.getName();
			}
		});
		double timeOffset = TAB.getInstance().getConfiguration().getConfig().getDouble("placeholders.time-offset", 0);
		SimpleDateFormat timeFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		manager.registerServerPlaceholder(new ServerPlaceholder("%time%", 500) {
			public Object get() {
				return timeFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000)));
			}
		});
		SimpleDateFormat dateFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		manager.registerServerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public Object get() {
				return dateFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000)));
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%ping%", 500) {
			public Object get(TabPlayer p) {
				return p.getPing();
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%player-version%", 100000000) {
			public Object get(TabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%player-version-id%", 100000000) {
			public Object get(TabPlayer p) {
				return p.getVersion().getNetworkId();
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%maxplayers%", 100000000) {
			public Object get() {
				return TAB.getInstance().getPlatform().getMaxPlayers();
			}
		});
		registerLuckPermsPlaceholders(manager);
		registerMemoryPlaceholders(manager);
		registerAnimationPlaceholders(manager);
		registerConditionPlaceholders(manager);
	}
	
	/**
	 * Evaluates inserted date format, returns default one and console error message if not valid
	 * @param value - date format to evaluate
	 * @param defaultValue - value to use if not valid
	 * @return evaluated date format
	 */
	private SimpleDateFormat createDateFormat(String value, String defaultValue) {
		try {
			return new SimpleDateFormat(value, Locale.ENGLISH);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
			return new SimpleDateFormat(defaultValue);
		}
	}

	/**
	 * Registers all memory-related placeholders
	 */
	private void registerMemoryPlaceholders(PlaceholderManager manager) {
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public Object get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576));
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-max%", 100000000) {
			public Object get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576));
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public Object get() {
				return decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024);
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-max-gb%", 100000000) {
			public Object get() {
				return decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024);
			}
		});
	}

	/**
	 * Registers luckperms placeholders
	 */
	private void registerLuckPermsPlaceholders(PlaceholderManager manager) {
		if (TAB.getInstance().getPermissionPlugin() instanceof LuckPerms) {
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 1000) {
				public Object get(TabPlayer p) {
					return ((LuckPerms)TAB.getInstance().getPermissionPlugin()).getPrefix(p);
				}
			});
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 1000) {
				public Object get(TabPlayer p) {
					return ((LuckPerms)TAB.getInstance().getPermissionPlugin()).getSuffix(p);
				}
			});
		}
	}

	/**
	 * Registers animations
	 */
	private void registerAnimationPlaceholders(PlaceholderManager manager) {
		for (Object s : TAB.getInstance().getConfiguration().getAnimationFile().getValues().keySet()) {
			Animation a = new Animation(s.toString(), TAB.getInstance().getConfiguration().getAnimationFile().getStringList(s + ".texts"), TAB.getInstance().getConfiguration().getAnimationFile().getInt(s + ".change-interval", 0));
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%animation:" + a.getName() + "%", 50) {

				public Object get(TabPlayer p) {
					return a.getMessage();
				}
				
				@Override
				public Collection<String> getNestedPlaceholders(String output) {
					return a.getNestedPlaceholders();
				}
			});
		}
	}

	/**
	 * Registers conditions
	 */
	@SuppressWarnings("unchecked")
	private void registerConditionPlaceholders(PlaceholderManager manager) {
		Condition.setConditions(new HashMap<>());
		Map<String, Map<Object, Object>> conditions = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("conditions");
		for (Entry<String, Map<Object, Object>> condition : conditions.entrySet()) {
			List<String> list = (List<String>) condition.getValue().get("conditions");
			String type = String.valueOf(condition.getValue().get("type"));
			String yes = String.valueOf(condition.getValue().get(true));
			String no = String.valueOf(condition.getValue().get(false));
			Condition c = Condition.compile(condition.getKey(), list, type, yes, no);
			Condition.getConditions().put(condition.getKey(), c);
			String identifier = "%condition:" + c.getName() + "%";
			PlaceholderManagerImpl pm = (PlaceholderManagerImpl) TAB.getInstance().getPlaceholderManager();
			int refresh = TAB.getInstance().getConfiguration().getConfig().getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
			if (pm.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
				refresh = pm.getPlayerPlaceholderRefreshIntervals().get(identifier);
			}
			//adding placeholders in conditions to the map so they are actually refreshed if not used anywhere else
			List<String> placeholdersInConditions = new ArrayList<>();
			for (String subcondition : list) {
				placeholdersInConditions.addAll(pm.detectPlaceholders(subcondition));
			}
			placeholdersInConditions.addAll(pm.detectPlaceholders(yes));
			placeholdersInConditions.addAll(pm.detectPlaceholders(no));
			pm.addUsedPlaceholders(placeholdersInConditions);
			manager.registerPlayerPlaceholder(new PlayerPlaceholder(identifier, refresh) {

				@Override
				public Object get(TabPlayer p) {
					return c.getText(p);
				}
			});
		}
	}
}