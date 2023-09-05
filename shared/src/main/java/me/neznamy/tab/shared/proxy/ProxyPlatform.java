package me.neznamy.tab.shared.proxy;

import lombok.Getter;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.proxy.features.unlimitedtags.ProxyNameTagX;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class containing common variables and methods
 * shared between proxies.
 */
public abstract class ProxyPlatform<T> implements Platform<T> {

    /** Plugin message handler for sending and receiving plugin messages */
    @Getter protected final PluginMessageHandler pluginMessageHandler = new PluginMessageHandler();

    /** Placeholders which are refreshed on backend server */
    @Getter private final Map<String, Integer> bridgePlaceholders = new ConcurrentHashMap<>();

    @Override
    public @NotNull GroupManager detectPermissionPlugin() {
        if (LuckPermsHook.getInstance().isInstalled() &&
                !TAB.getInstance().getConfiguration().isBukkitPermissions()) {
            return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
        }
        return new GroupManager("Vault through Bridge", TabPlayer::getGroup);
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
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.TPS, -1,
                () -> "\"tps\" is a backend-only placeholder as the proxy does not tick anything. If you wish to display TPS of " +
                        "the server player is connected to, use placeholders from PlaceholderAPI and install TAB-Bridge for forwarding support to the proxy.");
        new UniversalPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @NotNull NameTag getUnlimitedNameTags() {
        return new ProxyNameTagX();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerList() { return null; }

    public @NotNull TabExpansion createTabExpansion() {
        return new ProxyTabExpansion();
    }

    @Override
    public ProtocolVersion getServerVersion() {
        return ProtocolVersion.PROXY;
    }

    /**
     * Registers plugin's plugin message channel
     */
    public abstract void registerChannel();

}
