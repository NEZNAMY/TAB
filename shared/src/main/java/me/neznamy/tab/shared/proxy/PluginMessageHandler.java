package me.neznamy.tab.shared.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Universal interface for proxy to manage plugin messages
 */
public class PluginMessageHandler {

    private final Map<String, BiConsumer<ProxyTabPlayer, ByteArrayDataInput>> messages = new HashMap<>();

    public PluginMessageHandler() {
        messages.put("Placeholder", this::placeholder);
        messages.put("Vanished", this::vanished);
        messages.put("Disguised", this::disguised);
        messages.put("Invisible", this::invisible);
        messages.put("World", this::world);
        messages.put("Group", this::group);
        messages.put("Boat", this::boat);
        messages.put("Permission", this::permission);
        messages.put("PlayerJoinResponse", this::playerJoinResponse);
        messages.put("RegisterPlaceholder", this::registerPlaceholder);
        messages.put("PlaceholderError", this::placeholderError);
        messages.put("UpdateGameMode", this::updateGameMode);
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
        BiConsumer<ProxyTabPlayer, ByteArrayDataInput> handler = messages.get(in.readUTF());
        if (handler != null) handler.accept(player, in);
    }

    public void placeholder(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        String identifier = in.readUTF();
        if (!TAB.getInstance().getPlaceholderManager().getRegisteredPlaceholders().containsKey(identifier)) return;
        Placeholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
        if (placeholder instanceof RelationalPlaceholder) {
            TabPlayer other = TAB.getInstance().getPlayer(in.readUTF());
            String value = in.readUTF();
            if (other != null) { // Backend player did not connect via this proxy if null
                ((RelationalPlaceholder)placeholder).updateValue(player, other, value);
            }
        } else {
            ((PlayerPlaceholder)placeholder).updateValue(player, in.readUTF());
        }
    }

    public void vanished(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        player.setVanished(in.readBoolean());
        TAB.getInstance().getFeatureManager().onVanishStatusChange(player);
        ((PlayerPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.VANISHED)).updateValue(player, player.isVanished());
    }

    public void disguised(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        player.setDisguised(in.readBoolean());
    }

    public void invisible(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        player.setInvisibilityPotion(in.readBoolean());
    }

    public void world(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), in.readUTF());
    }

    public void group(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP)).updateValue(player, in.readUTF());
    }

    public void boat(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        player.setOnBoat(in.readBoolean());
    }

    public void permission(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        player.setHasPermission(in.readUTF(), in.readBoolean());
    }

    public void playerJoinResponse(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), in.readUTF());
        if (TAB.getInstance().getGroupManager().getPermissionPlugin().contains("Vault") &&
                !TAB.getInstance().getGroupManager().isGroupsByPermissions()) player.setGroup(in.readUTF());
        // reset attributes from previous server to default false values, new server will send separate update packets if needed
        player.setVanished(false);
        player.setDisguised(false);
        player.setInvisibilityPotion(false);
        int placeholderCount = in.readInt();
        for (int i=0; i<placeholderCount; i++) {
            String identifier = in.readUTF();
            if (identifier.startsWith("%rel_")) {
                int playerCount = in.readInt();
                for (int j=0; j<playerCount; j++) {
                    TabPlayer other = TAB.getInstance().getPlayer(in.readUTF());
                    String value = in.readUTF();
                    if (other != null) { // Backend player did not connect via this proxy if null
                        ((RelationalPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier))
                                .updateValue(player, other, value);
                    }
                }
            } else {
                Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
                if (pl instanceof PlayerPlaceholder) {
                    ((PlayerPlaceholder) pl).updateValue(player, in.readUTF());
                } else {
                    ((ServerPlaceholder) pl).updateValue(in.readUTF());
                }
            }
        }
        player.setGamemode(in.readInt());
        player.setBridgeConnected(true);
    }

    public void registerPlaceholder(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(in.readUTF()));
    }

    public void placeholderError(@NotNull ProxyTabPlayer player, @NotNull ByteArrayDataInput in) {
        String message = in.readUTF();
        int count = in.readInt();
        List<String> stack = new ArrayList<>();
        for (int i=0; i<count; i++) {
            stack.add(in.readUTF());
        }
        TAB.getInstance().getErrorManager().placeholderError(message, stack);
    }

    public void updateGameMode(ProxyTabPlayer player, ByteArrayDataInput in) {
        player.setGamemode(in.readInt());
    }
}
