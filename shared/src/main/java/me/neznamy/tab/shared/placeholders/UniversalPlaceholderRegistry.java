package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
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
import java.util.regex.Pattern;

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
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.BEDROCK, p -> Boolean.toString(p.isBedrockPlayer()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER, me.neznamy.tab.api.TabPlayer::getName);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.UUID, p -> p.getUniqueId().toString());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.WORLD, p -> ((TabPlayer)p).world.getName());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.SERVER, p -> ((TabPlayer)p).server.getName());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION, p -> ((TabPlayer)p).getVersion().getFriendlyName());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PLAYER_VERSION_ID, p -> PerformanceUtil.toString(((TabPlayer)p).getVersionId()));

        // Server
        manager.registerServerPlaceholder("%%", () -> "%");
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX, () -> PerformanceUtil.toString((int) (Runtime.getRuntime().maxMemory()/1024/1024)));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_MAX_GB, () -> decimal2.format((float)Runtime.getRuntime().maxMemory()/1024/1024/1024));
        if (!LuckPermsHook.getInstance().isInstalled()) {
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIXES, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIXES, -1, () -> "");
            manager.registerServerPlaceholder(TabConstants.Placeholder.LUCKPERMS_WEIGHT, -1, () -> "");
        }
    }

    private void registerServerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
        PlaceholdersConfiguration placeholders = TAB.getInstance().getConfiguration().getConfig().getPlaceholders();
        manager.registerServerPlaceholder(TabConstants.Placeholder.TIME, () -> placeholders.getTimeFormat().format(new Date(System.currentTimeMillis() + (int)(placeholders.getTimeOffset() *3600000))));
        manager.registerServerPlaceholder(TabConstants.Placeholder.DATE, () -> placeholders.getDateFormat().format(new Date(System.currentTimeMillis() + (int)(placeholders.getTimeOffset() *3600000))));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_USED, () -> PerformanceUtil.toString((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024)));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MEMORY_USED_GB, () -> decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024));
        manager.registerServerPlaceholder(TabConstants.Placeholder.ONLINE, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished()) count++;
            }
            ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
            if (proxy != null) {
                for (ProxyPlayer player : proxy.getProxyPlayers().values()) {
                    if (!player.isVanished()) count++;
                }
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerServerPlaceholder(TabConstants.Placeholder.STAFF_ONLINE, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.isVanished() && player.hasPermission(TabConstants.Permission.STAFF)) count++;
            }
            ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
            if (proxy != null) {
                for (ProxyPlayer player : proxy.getProxyPlayers().values()) {
                    if (!player.isVanished() && player.isStaff()) count++;
                }
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerServerPlaceholder(TabConstants.Placeholder.NON_STAFF_ONLINE, () -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (!player.hasPermission(TabConstants.Permission.STAFF) && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerServerPlaceholder(Pattern.compile("%online_(.+?)%"), 1000, matcher -> {
            Server server = Server.byName(matcher.group(1));
            return () -> {
                int count = 0;
                for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                    if (player.server == server && !player.isVanished()) {
                        count++;
                    }
                }
                ProxySupport proxySupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
                if (proxySupport != null) {
                    for (ProxyPlayer player : proxySupport.getProxyPlayers().values()) {
                        if (player.server == server && !player.isVanished()) count++;
                    }
                }
                return PerformanceUtil.toString(count);
            };
        });
    }

    private void registerPlayerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
        boolean proxy = TAB.getInstance().getPlatform().isProxy();
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.GROUP, me.neznamy.tab.api.TabPlayer::getGroup);
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.PING, p -> PerformanceUtil.toString(((TabPlayer)p).getPing()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.VANISHED, p -> Boolean.toString(((TabPlayer)p).isVanished()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.WORLD_ONLINE, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).world == player.world && !player.isVanished()) count++;
            }
            return PerformanceUtil.toString(count);
        });
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.SERVER_ONLINE, p -> {
            int count = 0;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                if (((TabPlayer)p).server == player.server && !player.isVanished()) count++;
            }
            ProxySupport proxySupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
            if (proxySupport != null) {
                for (ProxyPlayer player : proxySupport.getProxyPlayers().values()) {
                    if (((TabPlayer)p).server == player.server && !player.isVanished()) count++;
                }
            }
            return PerformanceUtil.toString(count);
        });
        if (proxy) {
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.GAMEMODE, -1, p -> PerformanceUtil.toString(((TabPlayer)p).getGamemode()));
        } else {
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.GAMEMODE, p -> PerformanceUtil.toString(((TabPlayer)p).getGamemode()));
        }
        if (LuckPermsHook.getInstance().isInstalled()) {
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIX,
                    p -> LuckPermsHook.getInstance().getPrefix((TabPlayer) p));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_PREFIXES,
                    p -> LuckPermsHook.getInstance().getPrefixes((TabPlayer) p));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIX,
                    p -> LuckPermsHook.getInstance().getSuffix((TabPlayer) p));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_SUFFIXES,
                    p -> LuckPermsHook.getInstance().getSuffixes((TabPlayer) p));
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.LUCKPERMS_WEIGHT,
                    p -> PerformanceUtil.toString(LuckPermsHook.getInstance().getWeight((TabPlayer) p)));
        }
        for (Entry<String, AnimationDefinition> entry : TAB.getInstance().getConfiguration().getAnimations().getAnimations().getAnimations().entrySet()) {
            Animation a = new Animation(manager, entry.getKey(), entry.getValue());
            manager.registerPlayerPlaceholder(TabConstants.Placeholder.animation(a.getName()), a.getRefresh(), p -> a.getMessage());
        }
        for (Entry<String, ConditionDefinition> condition : TAB.getInstance().getConfiguration().getConfig().getConditions().getConditions().entrySet()) {
            Condition c = new Condition(condition.getValue());
            TAB.getInstance().getPlaceholderManager().getConditionManager().registerCondition(c);
        }
        manager.getConditionManager().finishSetups();
    }
}
