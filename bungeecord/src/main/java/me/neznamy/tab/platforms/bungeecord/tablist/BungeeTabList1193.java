package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * TabList handler for 1.19.3+ players using the new tab list packets.
 * Because BungeeCord does not have a TabList API, we need to use packets.
 */
public class BungeeTabList1193 extends BungeeTabList {

    private static final EnumSet<PlayerListItemUpdate.Action> updateDisplayName = EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME);
    private static final EnumSet<PlayerListItemUpdate.Action> updateLatency = EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY);
    private static final EnumSet<PlayerListItemUpdate.Action> updateGameMode = EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE);
    private static final EnumSet<PlayerListItemUpdate.Action> updateListed = EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LISTED);
    private static final EnumSet<PlayerListItemUpdate.Action> updateListOrder = EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LIST_ORDER);
    private static final EnumSet<PlayerListItemUpdate.Action> updateHat = EnumSet.of(PlayerListItemUpdate.Action.UPDATE_HAT);

    // All actions for 1.19.3 - 1.21.1
    private static final EnumSet<PlayerListItemUpdate.Action> addPlayer_legacy = EnumSet.of(
            PlayerListItemUpdate.Action.ADD_PLAYER,
            PlayerListItemUpdate.Action.UPDATE_GAMEMODE,
            PlayerListItemUpdate.Action.UPDATE_LISTED,
            PlayerListItemUpdate.Action.UPDATE_LATENCY,
            PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME
    );

    // All actions for 1.21.2 - 1.21.3
    private static final EnumSet<PlayerListItemUpdate.Action> addPlayer_1_21_2 = EnumSet.of(
            PlayerListItemUpdate.Action.ADD_PLAYER,
            PlayerListItemUpdate.Action.UPDATE_GAMEMODE,
            PlayerListItemUpdate.Action.UPDATE_LISTED,
            PlayerListItemUpdate.Action.UPDATE_LATENCY,
            PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME,
            PlayerListItemUpdate.Action.UPDATE_LIST_ORDER
    );

    // All actions for 1.21.4+
    private static final EnumSet<PlayerListItemUpdate.Action> addPlayer_1_21_4 = EnumSet.of(
            PlayerListItemUpdate.Action.ADD_PLAYER,
            PlayerListItemUpdate.Action.UPDATE_GAMEMODE,
            PlayerListItemUpdate.Action.UPDATE_LISTED,
            PlayerListItemUpdate.Action.UPDATE_LATENCY,
            PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME,
            PlayerListItemUpdate.Action.UPDATE_LIST_ORDER,
            PlayerListItemUpdate.Action.UPDATE_HAT
    );

    /**
     * Constructs new instance with given parameter.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public BungeeTabList1193(@NonNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        removeUuid(entry);
        PlayerListItemRemove remove = new PlayerListItemRemove();
        remove.setUuids(new UUID[]{entry});
        player.sendPacket(remove);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        Item item = item(entry);
        if (displayName != null) item.setDisplayName(toComponent(displayName));
        sendPacket(updateDisplayName, item);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        Item item = item(entry);
        item.setPing(latency);
        sendPacket(updateLatency, item);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(updateGameMode, item);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        Item item = item(entry);
        item.setListed(listed);
        sendPacket(updateListed, item);
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        if (player.getVersionId() < ProtocolVersion.V1_21_2.getNetworkId()) return;
        Item item = item(entry);
        item.setListOrder(listOrder);
        sendPacket(updateListOrder, item);
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        if (player.getVersionId() < ProtocolVersion.V1_21_4.getNetworkId()) return;
        Item item = item(entry);
        item.setShowHat(showHat);
        sendPacket(updateHat, item);
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        addUuid(entry.getUniqueId());
        EnumSet<PlayerListItemUpdate.Action> actions;
        if (player.getVersionId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
            actions = addPlayer_1_21_4;
        } else if (player.getVersionId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
            actions = addPlayer_1_21_2;
        } else {
            actions = addPlayer_legacy;
        }
        sendPacket(actions, entryToItem(entry));
    }

    private void sendPacket(@NonNull EnumSet<PlayerListItemUpdate.Action> actions, @NonNull Item item) {
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(actions);
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
