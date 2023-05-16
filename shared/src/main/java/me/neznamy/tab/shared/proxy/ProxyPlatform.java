package me.neznamy.tab.shared.proxy;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.proxy.features.unlimitedtags.ProxyNameTagX;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class containing common variables and methods
 * shared between proxies.
 */
public abstract class ProxyPlatform implements Platform {

    /** Plugin message handler for sending and receiving plugin messages */
    @Getter protected final PluginMessageHandler pluginMessageHandler = new PluginMessageHandler();

    /** Placeholders which are refreshed on backend server */
    @Getter private final Map<String, Integer> bridgePlaceholders = new ConcurrentHashMap<>();

    @Getter private final TabFeature perWorldPlayerlist = null;
    @Getter private final ProxyTabExpansion tabExpansion = new ProxyTabExpansion();

    @Override
    public @NotNull PermissionPlugin detectPermissionPlugin() {
        if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
            return new VaultBridge();
        } else if (ReflectionUtils.classExists("net.luckperms.api.LuckPerms")) {
            return new LuckPerms();
        } else {
            return new VaultBridge();
        }
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        //internal dynamic %online_<server>% placeholder
        if (identifier.startsWith("%online_")) {
            String server = identifier.substring(8, identifier.length()-1);
            pl.registerServerPlaceholder(identifier, 1000, () ->
                    Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> p.getServer().equals(server) && !p.isVanished()).count());
            return;
        }
        Placeholder placeholder;
        int refresh = pl.getRefreshInterval(identifier);
        if (identifier.startsWith("%rel_")) {
            placeholder = pl.registerRelationalPlaceholder(identifier, -1, (viewer, target) -> null);
        } else {
            placeholder = pl.registerPlayerPlaceholder(identifier, -1, player -> null);
        }
        bridgePlaceholders.put(placeholder.getIdentifier(), refresh);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            ((ProxyTabPlayer)all).sendPluginMessage("Placeholder", placeholder.getIdentifier(), refresh);
        }
    }

    @Override
    public void registerPlaceholders() {
        new UniversalPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @NotNull NameTag getUnlimitedNametags() {
        return new ProxyNameTagX();
    }
}
