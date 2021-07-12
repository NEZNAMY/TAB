package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
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
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%"+TAB.getInstance().getPlatform().getSeparatorType()+"%", 1000) {
			public String get(TabPlayer p) {
				return p.getWorldName();
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%"+TAB.getInstance().getPlatform().getSeparatorType()+"online%", 1000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) count++;
				}
				return String.valueOf(count);
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%player%", 100000000) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		double timeOffset = TAB.getInstance().getConfiguration().getConfig().getDouble("placeholders.time-offset", 0);
		SimpleDateFormat timeFormat = TAB.getInstance().getErrorManager().createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		manager.registerServerPlaceholder(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return timeFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000)));
			}
		});
		SimpleDateFormat dateFormat = TAB.getInstance().getErrorManager().createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		manager.registerServerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return dateFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000)));
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%ping%", 50) {
			public String get(TabPlayer p) {
				return String.valueOf(p.getPing());
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%player-version%", 100000000) {
			public String get(TabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%player-version-id%", 100000000) {
			public String get(TabPlayer p) {
				return String.valueOf(p.getVersion().getNetworkId());
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%maxplayers%", 100000000) {
			public String get() {
				return String.valueOf(TAB.getInstance().getPlatform().getMaxPlayers());
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%%", 100000000) {
			public String get() {
				return "%";
			}
		});
		registerLuckPermsPlaceholders(manager);
		registerMemoryPlaceholders(manager);
		registerAnimationPlaceholders(manager);
		registerConditionPlaceholders(manager);
	}

	/**
	 * Registers all memory-related placeholders
	 */
	private void registerMemoryPlaceholders(PlaceholderManager manager) {
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return String.valueOf(((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)));
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-max%", 100000000) {
			public String get() {
				return String.valueOf(((int) (Runtime.getRuntime().maxMemory() / 1048576)));
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return String.valueOf(decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%memory-max-gb%", 100000000) {
			public String get() {
				return String.valueOf(decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024));
			}
		});
	}

	/**
	 * Registers luckperms placeholders
	 */
	private void registerLuckPermsPlaceholders(PlaceholderManager manager) {
		if (TAB.getInstance().getPermissionPlugin() instanceof LuckPerms) {
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)TAB.getInstance().getPermissionPlugin()).getPrefix(p);
				}
			});
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(TabPlayer p) {
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
			manager.registerServerPlaceholder(new ServerPlaceholder("%animation:" + a.getName() + "%", 50) {

				public String get() {
					return a.getMessage();
				}
			});
		}
	}

	/**
	 * Registers conditions
	 */
	private void registerConditionPlaceholders(PlaceholderManager manager) {
		Condition.setConditions(new HashMap<>());
		for (Object condition : TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("conditions").keySet()) {
			List<String> list = TAB.getInstance().getConfiguration().getConfig().getStringList("conditions." + condition + ".conditions"); //lol
			String type = TAB.getInstance().getConfiguration().getConfig().getString("conditions." + condition + ".type");
			String yes = TAB.getInstance().getConfiguration().getConfig().getString("conditions." + condition + ".true");
			String no = TAB.getInstance().getConfiguration().getConfig().getString("conditions." + condition + ".false");
			Condition c = Condition.compile(condition.toString(), list, type, yes, no);
			Condition.getConditions().put(condition.toString(), c);
			String identifier = "%condition:" + c.getName() + "%";
			PlaceholderManagerImpl pm = (PlaceholderManagerImpl) TAB.getInstance().getPlaceholderManager();
			pm.getAllUsedPlaceholderIdentifiers().add(identifier);
			int refresh = TAB.getInstance().getConfiguration().getConfig().getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
			if (pm.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
				refresh = pm.getPlayerPlaceholderRefreshIntervals().get(identifier);
			}
			manager.registerPlayerPlaceholder(new PlayerPlaceholder(identifier, refresh) {

				@Override
				public String get(TabPlayer p) {
					return c.getText(p);
				}
			});
		}
	}
}