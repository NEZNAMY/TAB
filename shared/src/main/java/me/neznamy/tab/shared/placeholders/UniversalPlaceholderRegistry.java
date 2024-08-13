package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.files.config.ConditionsSection.ConditionDefinition;
import me.neznamy.tab.shared.config.files.config.PlaceholdersConfiguration;
import me.neznamy.tab.shared.config.files.animations.AnimationConfiguration.AnimationDefinition;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Map.Entry;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 * which work on all platforms.
 */
@Getter
public class UniversalPlaceholderRegistry {

    /** Decimal formatter for 2 decimal places */
    private final DecimalFormat decimal2;

    /**
     * Constructs new instance.
     */
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
    public void registerPlaceholders(@NotNull PlaceholderManager manager) {
        registerConstants(manager);
        registerServerPlaceholders(manager);
        registerPlayerPlaceholders(manager);
    }

    private void registerConstants(@NotNull PlaceholderManager manager) {
        // Player
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.BEDROCK, -1, p -> Boolean.toString(((TabPlayer)p).isBedrockPlayer()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER, -1, me.neznamy.tab.api.TabPlayer::getName);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.WORLD, -1, p -> ((TabPlayer)p).world);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.SERVER, -1, p -> ((TabPlayer)p).server);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION, -1, p -> ((TabPlayer)p).getVersion().getFriendlyName());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION_ID, -1, p -> PerformanceUtil.toString(((TabPlayer)p).getVersion().getNetworkId()));

        // Server
        manager.registerServerPlaceholder("%%", -1, () -> "%");
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX, -1, () -> PerformanceUtil.toString((int) (Runtime.getRuntime().maxMemory()/1024/1024)));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX_GB, -1, () -> decimal2.format((float)Runtime.getRuntime().maxMemory()/1024/1024/1024));
        if (!LuckPermsHook.getInstance().isInstalled()) {
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, -1, () -> "");
        }
    }

    private void registerServerPlaceholders(@NotNull PlaceholderManager manager) {
        PlaceholdersConfiguration placeholders = TAB.getInstance().getConfiguration().getConfig().getPlaceholders();
        manager.registerServerPlaceholder(TabConstants.Placeholder.TIME, 500, () -> placeholders.timeFormat.format(new Date(System.currentTimeMillis() + (int)(placeholders.timeOffset*3600000))));
        manager.registerServerPlaceholder(TabConstants.Placeholder.DATE, 60000, () -> placeholders.dateFormat.format(new Date(System.currentTimeMillis() + (int)(placeholders.timeOffset*3600000))));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_USED, 200, () -> PerformanceUtil.toString((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024)));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_USED_GB, 200, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
        manager.registerServerPlaceholder(TabConstants.Placeholder.ONLINE, 1000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, 2000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerServerPlaceholder(TabConstants.Placeholder.NON_STAFF_ONLINE, 2000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
    }

    private void registerPlayerPlaceholders(@NotNull PlaceholderManager manager) {
        boolean proxy = TAB.getInstance().getPlatform().isProxy();
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.GROUP, -1, me.neznamy.tab.api.TabPlayer::getGroup);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PING, 500, p -> PerformanceUtil.toString(((TabPlayer)p).getPing()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.VANISHED, 1000, p -> Boolean.toString(((TabPlayer)p).isVanished()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.WORLD_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).world.equals(player.world) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.SERVER_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).server.equals(player.server) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.GAMEMODE, proxy ? -1 : 100, p -> PerformanceUtil.toString(((TabPlayer)p).getGamemode()));
        if (LuckPermsHook.getInstance().isInstalled()) {
            int refresh = TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval();
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, refresh,
                    p -> LuckPermsHook.getInstance().getPrefix((TabPlayer) p));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, refresh,
                    p -> LuckPermsHook.getInstance().getSuffix((TabPlayer) p));
        }
        for (Entry<String, AnimationDefinition> entry : TAB.getInstance().getConfiguration().getAnimations().getAnimations().animations.entrySet()) {
            Animation a = new Animation((PlaceholderManagerImpl) manager, entry.getKey(), entry.getValue());
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.animation(a.getName()), a.getRefresh(), p -> a.getMessage());
        }
        Condition.clearConditions();
        for (Entry<String, ConditionDefinition> condition : TAB.getInstance().getConfiguration().getConfig().getConditions().conditions.entrySet()) {
            ConditionDefinition def = condition.getValue();
            Condition c = new Condition(def.type, condition.getKey(), def.conditions, def.yes, def.no);
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.condition(c.getName()), c.getRefresh(), p -> c.getText((TabPlayer)p));
        }
        Condition.finishSetups();
    }
}