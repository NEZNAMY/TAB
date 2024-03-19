package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.tab.ServerUnique;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Abstract TabList class for BungeeCord containing
 * common code for all implementations.
 */
public abstract class BungeeTabList extends TabList<BungeeTabPlayer, BaseComponent> {

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
    protected BungeeTabList(@NonNull BungeeTabPlayer player) {
        super(player);
        uuids = (Collection<UUID>) ReflectionUtils.getField(ServerUnique.class, "uuids").get(((UserConnection)player.getPlayer()).getTabListHandler());
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.sendPacket(new PlayerListHeaderFooter(toComponent(header), toComponent(footer)));
    }

    /**
     * Creates new {@link Item} with given UUID.
     *
     * @param   id
     *          UUID to use
     * @return  New {@link Item} with given UUID.
     */
    @NonNull
    public Item item(@NonNull UUID id) {
        Item item = new Item();
        item.setUuid(id);
        return item;
    }

    /**
     * Converts entry data to item.
     *
     * @param   id
     *          Entry UUID
     * @param   name
     *          Entry name
     * @param   skin
     *          Entry skin
     * @param   latency
     *          Entry latency
     * @param   gameMode
     *          Entry game mode
     * @param   displayName
     *          Entry display name
     * @return  Converted item from parameters
     */
    @NonNull
    public Item entryToItem(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, int latency, int gameMode, @Nullable BaseComponent displayName) {
        Item item = item(id);
        item.setUsername(name);
        item.setDisplayName(displayName);
        item.setGamemode(gameMode);
        item.setListed(true);
        item.setPing(latency);
        if (skin != null) {
            item.setProperties(new Property[]{new Property(TEXTURES_PROPERTY, skin.getValue(), skin.getSignature())});
        } else {
            item.setProperties(new Property[0]);
        }
        return item;
    }

    /**
     * Adds given UUID to BungeeCord's tablist uuid tracker.
     *
     * @param   id
     *          UUID to add
     */
    public void addUuid(@NonNull UUID id) {
        uuids.add(id);
    }

    /**
     * Removes given UUID from BungeeCord's tablist uuid tracker.
     *
     * @param   id
     *          UUID to remove
     */
    public void removeUuid(@NonNull UUID id) {
        uuids.remove(id);
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (packet instanceof PlayerListItem) {
            PlayerListItem listItem = (PlayerListItem) packet;
            for (PlayerListItem.Item item : listItem.getItems()) {
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_DISPLAY_NAME || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    BaseComponent expectedDisplayName = getExpectedDisplayName(item.getUuid());
                    if (expectedDisplayName != null) item.setDisplayName(expectedDisplayName);
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
                    BaseComponent expectedDisplayName = getExpectedDisplayName(item.getUuid());
                    if (expectedDisplayName != null) item.setDisplayName(expectedDisplayName);
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

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return uuids.contains(entry);
    }

    @Override
    public BaseComponent toComponent(@NonNull TabComponent component) {
        return component.convert(player.getVersion());
    }
}
