package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for backend platforms with a few default implementations, as well as new methods.
 */
public interface BackendPlatform extends Platform {

    @Override
    @NotNull default GroupManager detectPermissionPlugin() {
        if (LuckPermsHook.getInstance().isInstalled()) {
            return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
        }
        return new GroupManager("None", p -> TabConstants.NO_GROUP);
    }

    default ProxySupport getProxySupport(@NotNull String plugin) { return null; }

    @Override
    default void registerPlaceholders() {
        UniversalPlaceholderRegistry registry = new UniversalPlaceholderRegistry();
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100,
                p -> PerformanceUtil.toString((int) Math.ceil(((BackendTabPlayer)p).getHealth())));
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500,
                p -> ((BackendTabPlayer)p).getDisplayName());
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.TPS, 1000,
                () -> registry.getDecimal2().format(Math.min(20, getTPS())));
        manager.registerInternalServerPlaceholder(TabConstants.Placeholder.MSPT, 1000,
                () -> registry.getDecimal2().format(getMSPT()));
        manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.DEATHS, 1000,
                p -> PerformanceUtil.toString(((BackendTabPlayer)p).getDeaths()));
        registry.registerPlaceholders(manager);
    }

    @Override
    default boolean isProxy() {
        return false;
    }

    @Override
    @NotNull
    default String getCommand() {
        return "tab";
    }

    /**
     * Registers a dummy placeholder implementation for specified identifier in case
     * no placeholder plugin was found.
     *
     * @param   identifier
     *          Placeholder identifier to register
     */
    default void registerDummyPlaceholder(@NotNull String identifier) {
        if (identifier.startsWith("%rel_")) { // To prevent placeholder identifier check from throwing
            TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, -1, (viewer, target) -> identifier);
        } else {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
        }
    }

    /**
     * Returns server's TPS for {@link TabConstants.Placeholder#TPS} placeholder
     *
     * @return  server's TPS
     */
    double getTPS();

    /**
     * Returns server's MSPT for {@link TabConstants.Placeholder#MSPT} placeholder
     *
     * @return  server's MSPT
     */
    double getMSPT();
}
