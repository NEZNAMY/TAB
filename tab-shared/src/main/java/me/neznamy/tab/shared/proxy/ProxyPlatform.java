package me.neznamy.tab.shared.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import me.neznamy.tab.shared.GroupManager;
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
import me.neznamy.tab.shared.proxy.message.incoming.*;
import me.neznamy.tab.shared.proxy.message.outgoing.RegisterPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Abstract class containing common variables and methods
 * shared between proxies.
 */
@Getter
public abstract class ProxyPlatform implements Platform {

    /** Registered plugin messages the plugin can receive from Bridge */
    private final Map<String, Supplier<IncomingMessage>> registeredMessages = new HashMap<>();

    /** Placeholders which are refreshed on backend server */
    private final Map<String, Integer> bridgePlaceholders = new ConcurrentHashMap<>();

    /**
     * Constructs new instance.
     */
    protected ProxyPlatform() {
        registeredMessages.put("PlaceholderError", PlaceholderError::new);
        registeredMessages.put("UpdateGameMode", UpdateGameMode::new);
        registeredMessages.put("Permission", HasPermission::new);
        registeredMessages.put("Invisible", Invisible::new);
        registeredMessages.put("Disguised", Disguised::new);
        registeredMessages.put("Boat", OnBoat::new);
        registeredMessages.put("World", SetWorld::new);
        registeredMessages.put("Group", SetGroup::new);
        registeredMessages.put("Vanished", Vanished::new);
        registeredMessages.put("Placeholder", UpdatePlaceholder::new);
        registeredMessages.put("PlayerJoinResponse", PlayerJoinResponse::new);
        registeredMessages.put("RegisterPlaceholder", me.neznamy.tab.shared.proxy.message.incoming.RegisterPlaceholder::new);
    }

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
            pl.registerServerPlaceholder(identifier, 1000, () -> {
                int count = 0;
                for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                    if (player.getServer().equals(server) && !player.isVanished()) count++;
                }
                return count;
            });
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
            ((ProxyTabPlayer)all).sendPluginMessage(new RegisterPlaceholder(placeholder.getIdentifier(), refresh));
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
    public boolean isProxy() {
        return true;
    }

    /**
     * Handles incoming plugin message with tab's channel name
     *
     * @param   uuid
     *          plugin message receiver
     * @param   bytes
     *          incoming message
     */
    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessage(@NotNull UUID uuid, byte[] bytes) {
        ProxyTabPlayer player = (ProxyTabPlayer) TAB.getInstance().getPlayer(uuid);
        if (player == null) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        Supplier<IncomingMessage> supplier = registeredMessages.get(in.readUTF());
        if (supplier != null) {
            IncomingMessage msg = supplier.get();
            msg.read(in);
            msg.process(player);
        }
    }

    /**
     * Registers plugin's plugin message channel
     */
    public abstract void registerChannel();
}
