package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.tab.ServerUnique;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 * Abstract TabList class for BungeeCord containing
 * common code for all implementations.
 */
public abstract class BungeeTabList implements TabList {

    /** Player this TabList belongs to */
    @NotNull
    protected final BungeeTabPlayer player;

    /** Pointer to UUIDs in player's TabList */
    private final Collection<UUID> uuids;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected BungeeTabList(@NotNull BungeeTabPlayer player) {
        this.player = player;
        uuids = (Collection<UUID>) ReflectionUtils.getField(ServerUnique.class, "uuids").get(((UserConnection)player.getPlayer()).getTabListHandler());
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull TabComponent header, @NotNull TabComponent footer) {
        player.sendPacket(new PlayerListHeaderFooter(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        ));
    }

    /**
     * Creates new {@link Item} with given UUID.
     *
     * @param   id
     *          UUID to use
     * @return  New {@link Item} with given UUID.
     */
    @NotNull
    public Item item(@NotNull UUID id) {
        Item item = new Item();
        item.setUuid(id);
        return item;
    }

    /**
     * Converts {@link Entry} to {@link Item}.
     *
     * @param   entry
     *          Entry to convert
     * @return  Converted {@link Item}
     */
    @NotNull
    public Item entryToItem(Entry entry) {
        Item item = item(entry.getUniqueId());
        if (entry.getDisplayName() != null) {
            item.setDisplayName(player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion()));
        }
        item.setGamemode(entry.getGameMode());
        item.setListed(true);
        item.setPing(entry.getLatency());
        if (entry.getSkin() != null) {
            item.setProperties(new Property[]{new Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature())});
        } else {
            item.setProperties(new Property[0]);
        }
        item.setUsername(entry.getName());
        return item;
    }

    /**
     * Adds given UUID to BungeeCord's tablist uuid tracker.
     *
     * @param   id
     *          UUID to add
     */
    public void addUuid(@NotNull UUID id) {
        uuids.add(id);
    }

    /**
     * Removes given UUID from BungeeCord's tablist uuid tracker.
     *
     * @param   id
     *          UUID to remove
     */
    public void removeUuid(@NotNull UUID id) {
        uuids.remove(id);
    }

    @Override
    public void onPacketSend(@NotNull Object packet) {
        if (packet instanceof PlayerListItem) {
            PlayerListItem listItem = (PlayerListItem) packet;
            for (PlayerListItem.Item item : listItem.getItems()) {
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_DISPLAY_NAME || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    TabComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(player, item.getUuid());
                    if (newDisplayName != null) item.setDisplayName(player.getPlatform().toComponent(newDisplayName, player.getVersion()));
                }
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_LATENCY || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    item.setPing(TAB.getInstance().getFeatureManager().onLatencyChange(player, item.getUuid(), item.getPing()));
                }
                if (listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, item.getUuid(), item.getUsername());
                }
            }
        } else if (packet instanceof PlayerListItemUpdate) {
            PlayerListItemUpdate update = (PlayerListItemUpdate) packet;
            for (PlayerListItem.Item item : update.getItems()) {
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME)) {
                    TabComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(player, item.getUuid());
                    if (newDisplayName != null) item.setDisplayName(player.getPlatform().toComponent(newDisplayName, player.getVersion()));
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_LATENCY)) {
                    item.setPing(TAB.getInstance().getFeatureManager().onLatencyChange(player, item.getUuid(), item.getPing()));
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.ADD_PLAYER)) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, item.getUuid(), item.getUsername());
                }
            }
        }
    }
}
