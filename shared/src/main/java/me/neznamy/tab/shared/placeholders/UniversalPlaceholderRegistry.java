package me.neznamy.tab.shared.placeholders;

import lombok.NonNull;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 * which work on all platforms.
 */
public class UniversalPlaceholderRegistry {

    /** Decimal formatter for 2 decimal places */
    private final DecimalFormat decimal2;

    public UniversalPlaceholderRegistry() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimal2 = new DecimalFormat("#.##", symbols);
    }

    /**
     * Registers all placeholders into placeholder manager
     *
     * @param   manager
     *          placeholder manager to register placeholders to
     */
    @SuppressWarnings("unchecked")
    public void registerPlaceholders(@NotNull PlaceholderManager manager) {
        manager.registerServerPlaceholder("%%", -1, () -> "%");
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.VANISHED, 1000, p -> ((TabPlayer)p).isVanished());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.WORLD, -1, p -> ((TabPlayer)p).getWorld());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.WORLD_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).getWorld().equals(player.getWorld()) && !player.isVanished()) count++;
            }
            return count;
        });
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.SERVER, -1, p -> ((TabPlayer)p).getServer());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.SERVER_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).getServer().equals(player.getServer()) && !player.isVanished()) count++;
            }
            return count;
        });
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER, -1, me.neznamy.tab.api.TabPlayer::getName);
        double timeOffset = TAB.getInstance().getConfiguration().getConfig().getDouble("placeholders.time-offset", 0);
        SimpleDateFormat timeFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
        manager.registerServerPlaceholder(TabConstants.Placeholder.TIME, 500, () -> timeFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000))));
        SimpleDateFormat dateFormat = createDateFormat(TAB.getInstance().getConfiguration().getConfig().getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
        manager.registerServerPlaceholder(TabConstants.Placeholder.DATE, 60000, () -> dateFormat.format(new Date(System.currentTimeMillis() + (int)(timeOffset*3600000))));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PING, 500, p -> ((TabPlayer)p).getPing());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION, -1, p -> ((TabPlayer)p).getVersion().getFriendlyName());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION_ID, -1, p -> ((TabPlayer)p).getVersion().getNetworkId());
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_USED, 200, () -> ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX, -1, () -> ((int) (Runtime.getRuntime().maxMemory() / 1048576)));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_USED_GB, 200, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX_GB, -1, () -> decimal2.format((float)Runtime.getRuntime().maxMemory()/1024/1024/1024));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.BEDROCK, -1, p -> ((TabPlayer)p).isBedrockPlayer());
        manager.registerServerPlaceholder(TabConstants.Placeholder.ONLINE, 1000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished()) count++;
            }
            return count;
        });
        manager.registerServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, 2000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return count;
        });
        manager.registerServerPlaceholder(TabConstants.Placeholder.NON_STAFF_ONLINE, 2000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return count;
        });
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.GAMEMODE, 100, p -> ((TabPlayer)p).getGamemode());
        if (LuckPermsHook.getInstance().isInstalled()) {
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, 1000,
                    p -> LuckPermsHook.getInstance().getPrefix((TabPlayer) p));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, 1000,
                    p -> LuckPermsHook.getInstance().getSuffix((TabPlayer) p));
        } else {
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, -1, () -> "");
        }
        for (Object s : TAB.getInstance().getConfiguration().getAnimationFile().getValues().keySet()) {
            Animation a = new Animation(
                    (PlaceholderManagerImpl) manager,
                    s.toString(),
                    TAB.getInstance().getConfiguration().getAnimationFile().getStringList(s + ".texts"),
                    TAB.getInstance().getConfiguration().getAnimationFile().getInt(s + ".change-interval", 0)
            );
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.animation(a.getName()), a.getRefresh(), p -> a.getMessage());
        }
        Condition.clearConditions();
        Map<String, Map<Object, Object>> conditions = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("conditions");
        for (Entry<String, Map<Object, Object>> condition : conditions.entrySet()) {
            List<String> list = (List<String>) condition.getValue().get("conditions");
            String type = String.valueOf(condition.getValue().get("type"));
            String yes = condition.getValue().getOrDefault(true, true).toString();
            String no = condition.getValue().getOrDefault(false, false).toString();
            if (list == null) {
                TAB.getInstance().getConfigHelper().startup().conditionHasNoConditions(condition.getKey());
                continue;
            }
            Condition c = new Condition(!"OR".equals(type), condition.getKey(), list, yes, no);
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.condition(c.getName()), c.getRefresh(), p -> c.getText((TabPlayer)p));
        }
        Condition.finishSetups();
    }

    public String format(double value) {
        return decimal2.format(value);
    }

    /**
     * Formats TPS using number formatter with 2 decimal places.
     *
     * @param   tps
     *          TPS to format
     * @return  Formatted TPS as a String
     */
    public String formatTPS(double tps) {
        return decimal2.format(Math.min(20, tps));
    }
    
    /**
     * Evaluates inserted date format. If it's not valid, a message is printed into console
     * and format with {@code defaultValue} is returned.
     *
     * @param   value
     *          date format to evaluate
     * @param   defaultValue
     *          value to use if entered format is not valid
     * @return  evaluated date format
     */
    private SimpleDateFormat createDateFormat(@NonNull String value, @NonNull String defaultValue) {
        try {
            return new SimpleDateFormat(value, Locale.ENGLISH);
        } catch (IllegalArgumentException e) {
            TAB.getInstance().getConfigHelper().startup().invalidDateFormat(value);
            return new SimpleDateFormat(defaultValue);
        }
    }
}