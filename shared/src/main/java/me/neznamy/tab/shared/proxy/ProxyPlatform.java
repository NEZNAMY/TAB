package me.neznamy.tab.shared.proxy;

import lombok.Getter;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.RegisterPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class containing common variables and methods
 * shared between proxies.
 */
@Getter
public abstract class ProxyPlatform implements Platform {

    @Override
    public @NotNull GroupManager detectPermissionPlugin() {
        if (LuckPermsHook.getInstance().isInstalled() &&
                !TAB.getInstance().getConfiguration().getConfig().isBukkitPermissions()) {
            return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
        }
        return new GroupManager("Vault through Bridge", TabPlayer::getGroup);
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        Placeholder placeholder;
        int refresh = pl.getConfiguration().getRefreshInterval(identifier);
        if (identifier.startsWith("%rel_")) {
            placeholder = pl.registerRelationalBridgePlaceholder(identifier, refresh);
        } else {
            placeholder = pl.registerBridgePlaceholder(identifier, refresh);
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            ((ProxyTabPlayer)all).sendPluginMessage(new RegisterPlaceholder(placeholder.getIdentifier(), refresh));
        }
    }

    @Override
    public void registerPlaceholders() {
        TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder(TabConstants.Placeholder.TPS, -1,
                () -> "\"tps\" is a backend-only placeholder as the proxy does not tick anything. If you wish to display TPS of " +
                        "the server player is connected to, use placeholders from PlaceholderAPI and install TAB-Bridge for forwarding support to the proxy.");
        new UniversalPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) { return null; }

    public @NotNull TabExpansion createTabExpansion() {
        return new ProxyTabExpansion();
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    /**
     * Registers plugin's plugin message channel
     */
    public abstract void registerChannel();
}
