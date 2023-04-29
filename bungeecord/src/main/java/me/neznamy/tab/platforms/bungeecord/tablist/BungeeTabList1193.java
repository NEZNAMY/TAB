package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * TabList handler for 1.19.3+ players using the new tablist packets.
 * Because BungeeCord does not have a TabList API, we need to use packets.
 * They are sent using an internal BungeeCord method that keeps track of them,
 * so they are removed on server switch to secure parity with Velocity.
 */
public class BungeeTabList1193 extends BungeeTabList {

    public BungeeTabList1193(BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        PlayerListItemRemove remove = new PlayerListItemRemove();
        remove.setUuids(new UUID[]{entry});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(remove);
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName) {
        Item item = item(entry);
        item.setDisplayName(displayName == null ? null : displayName.toString(player.getVersion()));
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME), item);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        Item item = item(entry);
        item.setPing(latency);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY), item);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE), item);
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        Item item = item(entry.getUniqueId());
        if (entry.getDisplayName() != null) item.setDisplayName(entry.getDisplayName().toString(player.getVersion()));
        item.setGamemode(entry.getGameMode());
        item.setListed(true);
        item.setPing(entry.getLatency());
        if (entry.getSkin() != null) {
            item.setProperties(new Property[]{new Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature())});
        } else {
            item.setProperties(new Property[0]);
        }
        item.setUsername(entry.getName());
        sendPacket(EnumSet.allOf(PlayerListItemUpdate.Action.class), item);
    }

    private void sendPacket(@NonNull EnumSet<PlayerListItemUpdate.Action> actions, @NonNull Item item) {
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(actions);
        packet.setItems(new Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }
}
