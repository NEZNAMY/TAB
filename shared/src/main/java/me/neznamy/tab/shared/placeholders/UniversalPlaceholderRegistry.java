package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 * which work on all platforms.
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

    /** Decimal formatter for 2 decimal places */
    private final DecimalFormat decimal2 = new DecimalFormat("#.##");

    @SuppressWarnings("unchecked")
    @Override
    public void registerPlaceholders(PlaceholderManager manager) {
        manager.registerServerPlaceholder("%%", -1, () -> "%").enableTriggerMode();
        manager.registerPlayerPlaceholder("%group%", -1, TabPlayer::getGroup).enableTriggerMode();
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
        manager.registerServerPlaceholder("%memory-used%", 200, () -> ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)));
        manager.registerServerPlaceholder("%memory-max%", -1, () -> ((int) (Runtime.getRuntime().maxMemory() / 1048576))).enableTriggerMode();
        manager.registerServerPlaceholder("%memory-used-gb%", 200, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
        manager.registerServerPlaceholder("%memory-max-gb%", -1, () -> decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024)).enableTriggerMode();
        manager.registerServerPlaceholder("%online%", 1000, () -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> !all.isVanished()).count());
        manager.registerServerPlaceholder("%staffonline%", 2000, () -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> all.hasPermission(TabConstants.Permission.STAFF) && !all.isVanished()).count());
        manager.registerServerPlaceholder("%nonstaffonline%", 2000, () -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> !all.hasPermission(TabConstants.Permission.STAFF) && !all.isVanished()).count());
        PermissionPlugin plugin = TAB.getInstance().getGroupManager().getPlugin();
        if (plugin instanceof LuckPerms) {
            manager.registerPlayerPlaceholder("%luckperms-prefix%", 1000, ((LuckPerms)plugin)::getPrefix);
            manager.registerPlayerPlaceholder("%luckperms-suffix%", 1000, ((LuckPerms)plugin)::getSuffix);
        }
        for (Object s : TAB.getInstance().getConfiguration().getAnimationFile().getValues().keySet()) {
            Animation a = new Animation(s.toString(), TAB.getInstance().getConfiguration().getAnimationFile().getStringList(s + ".texts"),
                    TAB.getInstance().getConfiguration().getAnimationFile().getInt(s + ".change-interval", 0));
            List<String> nested = Arrays.asList(a.getNestedPlaceholders());
            ((PlaceholderManagerImpl) manager).registerPlaceholder(new PlayerPlaceholderImpl("%animation:" + a.getName() + "%", a.getRefresh(), p -> a.getMessage()) {

                @Override
                public List<String> getNestedPlaceholders(String output) {
                    return nested;
                }
            });
        }
        Condition.clearConditions();
        Map<String, Map<Object, Object>> conditions = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("conditions");
        for (Entry<String, Map<Object, Object>> condition : conditions.entrySet()) {
            List<String> list = (List<String>) condition.getValue().get("conditions");
            String type = String.valueOf(condition.getValue().get("type"));
            String yes = condition.getValue().getOrDefault(true, true).toString();
            String no = condition.getValue().getOrDefault(false, false).toString();
            Condition c = new Condition(!"OR".equals(type), condition.getKey(), list, yes, no);
            String identifier = "%condition:" + c.getName() + "%";
            manager.registerPlayerPlaceholder(identifier, c.getRefresh(), c::getText);
        }
    }
    
    /**
     * Evaluates inserted date format. If it's not valid, a message is printed into console
     * and format with {@code defaultValue} is returned.
     *
     * @param    value
     *             date format to evaluate
     * @param    defaultValue
     *             value to use if entered format is not valid
     * @return    evaluated date format
     */
    private SimpleDateFormat createDateFormat(String value, String defaultValue) {
        try {
            return new SimpleDateFormat(value, Locale.ENGLISH);
        } catch (IllegalArgumentException e) {
            TAB.getInstance().getErrorManager().startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
            return new SimpleDateFormat(defaultValue);
        }
    }
}