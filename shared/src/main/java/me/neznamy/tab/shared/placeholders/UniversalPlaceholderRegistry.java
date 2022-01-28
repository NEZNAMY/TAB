package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");
	
	private Object luckPermsPrefixSub;
	private Object luckPermsSuffixSub;

	@SuppressWarnings("unchecked")
	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		manager.registerServerPlaceholder("%%", -1, () -> "%").enableTriggerMode();
		manager.registerPlayerPlaceholder("%vanished%", 1000, TabPlayer::isVanished);
		manager.registerPlayerPlaceholder("%world%", -1, TabPlayer::getWorld).enableTriggerMode();
		manager.registerPlayerPlaceholder("%worldonline%", 1000, p -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> p.getWorld().equals(all.getWorld()) && !all.isVanished()).count());
		manager.registerPlayerPlaceholder("%server%", -1, TabPlayer::getServer).enableTriggerMode();
		manager.registerPlayerPlaceholder("%serveronline%", 1000, p -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> p.getServer().equals(all.getServer()) && !all.isVanished()).count());
		manager.registerPlayerPlaceholder("%player%", -1, TabPlayer::getName).enableTriggerMode();
		double timeOffset = TAB.getInstance().getConfiguration().getConfig().getDouble("placeholders.time-offset", 0);
		SimpleDateFormat timeFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		manager.registerServerPlaceholder("%time%", 500, () -> timeFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000))));
		SimpleDateFormat dateFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		manager.registerServerPlaceholder("%date%", 60000, () -> dateFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000))));
		manager.registerPlayerPlaceholder("%ping%", 500, TabPlayer::getPing);
		manager.registerPlayerPlaceholder("%player-version%", -1, p -> p.getVersion().getFriendlyName()).enableTriggerMode();
		manager.registerPlayerPlaceholder("%player-version-id%", -1, p -> p.getVersion().getNetworkId()).enableTriggerMode();
		manager.registerServerPlaceholder("%maxplayers%", -1, () -> TAB.getInstance().getPlatform().getMaxPlayers()).enableTriggerMode();
		manager.registerServerPlaceholder("%memory-used%", 200, () -> ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)));
		manager.registerServerPlaceholder("%memory-max%", -1, () -> ((int) (Runtime.getRuntime().maxMemory() / 1048576))).enableTriggerMode();
		manager.registerServerPlaceholder("%memory-used-gb%", 200, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
		manager.registerServerPlaceholder("%memory-max-gb%", -1, () -> decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024)).enableTriggerMode();
		PermissionPlugin plugin = TAB.getInstance().getGroupManager().getPlugin();
		if (plugin instanceof LuckPerms) {
			PlayerPlaceholder prefix = manager.registerPlayerPlaceholder("%luckperms-prefix%", -1, ((LuckPerms)plugin)::getPrefix);
			prefix.enableTriggerMode(() ->
				luckPermsPrefixSub = LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
					TabPlayer p = TAB.getInstance().getPlayer(event.getUser().getUniqueId());
					if (p == null) return; //server still starting up and users connecting already (LP loading them)
					prefix.updateValue(p, prefix.request(p));
				}), () -> ((EventSubscription<UserDataRecalculateEvent>)luckPermsPrefixSub).close());
			PlayerPlaceholder suffix = manager.registerPlayerPlaceholder("%luckperms-suffix%", -1, ((LuckPerms)plugin)::getSuffix);
			suffix.enableTriggerMode(() ->
				luckPermsSuffixSub = LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
					TabPlayer p = TAB.getInstance().getPlayer(event.getUser().getUniqueId());
					if (p == null) return; //server still starting up and users connecting already (LP loading them)
					suffix.updateValue(p, suffix.request(p));
				}), () -> ((EventSubscription<UserDataRecalculateEvent>)luckPermsSuffixSub).close());
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
			((PlaceholderManagerImpl) manager).registerPlaceholder(new PlayerPlaceholderImpl("%animation:" + a.getName() + "%", a.getRefresh(), p -> a.getMessage()) {

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
			String yes = condition.getValue().getOrDefault(true, true).toString();
			String no = condition.getValue().getOrDefault(false, false).toString();
			Condition c = Condition.compile(condition.getKey(), list, type, yes, no);
			Condition.getConditions().put(condition.getKey(), c);
			String identifier = "%condition:" + c.getName() + "%";
			manager.registerPlayerPlaceholder(identifier, c.getRefresh(), c::getText);
		}
	}
}