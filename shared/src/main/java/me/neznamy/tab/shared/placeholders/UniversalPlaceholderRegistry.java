package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.animation.Animation;
import me.neznamy.tab.shared.placeholders.animation.AnimationConfiguration.AnimationDefinition;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.placeholders.conditions.ConditionsSection.ConditionDefinition;
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
    public void registerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
        registerConstants(manager);
        registerServerPlaceholders(manager);
        registerPlayerPlaceholders(manager);
    }

    private void registerConstants(@NotNull PlaceholderManagerImpl manager) {
        // Player
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.BEDROCK, -1, p -> Boolean.toString(p.isBedrockPlayer()));
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.PLAYER, -1, me.neznamy.tab.api.TabPlayer::getName);
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.UUID, -1, p -> p.getUniqueId().toString());
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.WORLD, -1, p -> ((TabPlayer)p).world.getName());
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.SERVER, -1, p -> ((TabPlayer)p).server.getName());
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION, -1, p -> ((TabPlayer)p).getVersion().getFriendlyName());
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION_ID, -1, p -> PerformanceUtil.toString(((TabPlayer)p).getVersionId()));

        // Server
        manager.registerInternalServerPlaceholder("%%", -1, () -> "%");
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX, -1, () -> PerformanceUtil.toString((int) (Runtime.getRuntime().maxMemory()/1024/1024)));
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX_GB, -1, () -> decimal2.format((float)Runtime.getRuntime().maxMemory()/1024/1024/1024));
        if (!LuckPermsHook.getInstance().isInstalled()) {
            manager.registerInternalServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, -1, () -> "");
            manager.registerInternalServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIXES, -1, () -> "");
            manager.registerInternalServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, -1, () -> "");
            manager.registerInternalServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIXES, -1, () -> "");
            manager.registerInternalServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_WEIGHT, -1, () -> "");
        }
    }

    private void registerServerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
        PlaceholdersConfiguration placeholders = TAB.getInstance().getConfiguration().getConfig().getPlaceholders();
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.TIME, 500, () -> placeholders.getTimeFormat().format(new Date(System.currentTimeMillis() + (int)(placeholders.getTimeOffset() *3600000))));
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.DATE, 60000, () -> placeholders.getDateFormat().format(new Date(System.currentTimeMillis() + (int)(placeholders.getTimeOffset() *3600000))));
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.MEMORY_USED, 200, () -> PerformanceUtil.toString((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024)));
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.MEMORY_USED_GB, 200, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.ONLINE, 1000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, 2000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.NON_STAFF_ONLINE, 2000, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
    }

    private void registerPlayerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
        boolean proxy = TAB.getInstance().getPlatform().isProxy();
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.GROUP, -1, me.neznamy.tab.api.TabPlayer::getGroup);
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.PING, 500, p -> PerformanceUtil.toString(((TabPlayer)p).getPing()));
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.VANISHED, 1000, p -> Boolean.toString(((TabPlayer)p).isVanished()));
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.WORLD_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).world == player.world && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.SERVER_ONLINE, 1000, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).server == player.server && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.GAMEMODE, proxy ? -1 : 100, p -> PerformanceUtil.toString(((TabPlayer)p).getGamemode()));
        if (LuckPermsHook.getInstance().isInstalled()) {
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, 1000,
                    p -> LuckPermsHook.getInstance().getPrefix((TabPlayer) p));
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIXES, 1000,
                    p -> LuckPermsHook.getInstance().getPrefixes((TabPlayer) p));
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, 1000,
                    p -> LuckPermsHook.getInstance().getSuffix((TabPlayer) p));
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIXES, 1000,
                    p -> LuckPermsHook.getInstance().getSuffixes((TabPlayer) p));
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_WEIGHT, 1000,
                    p -> PerformanceUtil.toString(LuckPermsHook.getInstance().getWeight((TabPlayer) p)));
        }
        for (Entry<String, AnimationDefinition> entry : TAB.getInstance().getConfiguration().getAnimations().getAnimations().getAnimations().entrySet()) {
            Animation a = new Animation(manager, entry.getKey(), entry.getValue());
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.animation(a.getName()), a.getRefresh(), p -> a.getMessage());
        }
        for (Entry<String, ConditionDefinition> condition : TAB.getInstance().getConfiguration().getConfig().getConditions().getConditions().entrySet()) {
            Condition c = new Condition(condition.getValue());
            manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.condition(c.getName()), c.getRefresh(), p -> c.getText((TabPlayer)p));
            TAB.getInstance().getPlaceholderManager().getConditionManager().registerCondition(c);
        }
        manager.getConditionManager().finishSetups();
    }
}
