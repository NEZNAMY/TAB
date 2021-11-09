package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder("%world%", 1000, TabPlayer::getWorld);
		manager.registerPlayerPlaceholder("%worldonline%", 1000, p -> {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
					if (String.valueOf(p.getWorld()).equals(all.getWorld())) count++;
				}
				return count;
		});
		manager.registerPlayerPlaceholder("%server%", 1000, TabPlayer::getServer);
		manager.registerPlayerPlaceholder("%serveronline%", 1000, p -> {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
					if (String.valueOf(p.getServer()).equals(all.getServer())) count++;
				}
				return count;
		});
		manager.registerPlayerPlaceholder("%player%", 100000000, TabPlayer::getName);
		double timeOffset = TAB.getInstance().getConfiguration().getConfig().getDouble("placeholders.time-offset", 0);
		SimpleDateFormat timeFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		manager.registerServerPlaceholder("%time%", 500, () -> timeFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000))));
		SimpleDateFormat dateFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		manager.registerServerPlaceholder("%date%", 60000, () -> dateFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000))));
		manager.registerPlayerPlaceholder("%ping%", 500, TabPlayer::getPing);
		manager.registerPlayerPlaceholder("%player-version%", 100000000, p -> p.getVersion().getFriendlyName());
		manager.registerPlayerPlaceholder("%player-version-id%", 100000000, p -> p.getVersion().getNetworkId());
		manager.registerServerPlaceholder("%maxplayers%", 100000000, () -> TAB.getInstance().getPlatform().getMaxPlayers());
		manager.registerServerPlaceholder("%memory-used%", 200, () -> ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)));
		manager.registerServerPlaceholder("%memory-max%", 100000000, () -> ((int) (Runtime.getRuntime().maxMemory() / 1048576)));
		manager.registerServerPlaceholder("%memory-used-gb%", 200, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
		manager.registerServerPlaceholder("%memory-max-gb%", 100000000, () -> decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024));
		PermissionPlugin plugin = TAB.getInstance().getGroupManager().getPlugin();
		if (plugin instanceof LuckPerms) {
			manager.registerPlayerPlaceholder("%luckperms-prefix%", 1000, ((LuckPerms)plugin)::getPrefix);
			manager.registerPlayerPlaceholder("%luckperms-suffix%", 1000, ((LuckPerms)plugin)::getSuffix);
		}
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
		} catch (IllegalArgumentException e) {
			TAB.getInstance().getErrorManager().startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
			return new SimpleDateFormat(defaultValue);
		}
	}

	/**
	 * Registers animations
	 */
	private void registerAnimationPlaceholders(PlaceholderManager manager) {
		for (Object s : TAB.getInstance().getConfiguration().getAnimationFile().getValues().keySet()) {
			Animation a = new Animation(s.toString(), TAB.getInstance().getConfiguration().getAnimationFile().getStringList(s + ".texts"), TAB.getInstance().getConfiguration().getAnimationFile().getInt(s + ".change-interval", 0));
			((PlaceholderManagerImpl) manager).registerPlaceholder(new PlayerPlaceholder("%animation:" + a.getName() + "%", 50, p -> a.getMessage()) {

				@Override
				public String[] getNestedPlaceholders(String output) {
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
			manager.registerPlayerPlaceholder(identifier, c.getRefresh(), c::getText);
		}
	}
}