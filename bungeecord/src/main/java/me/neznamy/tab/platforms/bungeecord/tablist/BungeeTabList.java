package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
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
public abstract class BungeeTabList extends TrackedTabList<BungeeTabPlayer> {

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
    @NotNull
    public Item item(@NonNull UUID id) {
        Item item = new Item();
        item.setUuid(id);
        return item;
    }

    /**
     * Converts entry to item.
     *
     * @param   entry
     *          Entry to convert
     * @return  Converted item from entry
     */
    @NotNull
    public Item entryToItem(@NonNull Entry entry) {
        Item item = item(entry.getUniqueId());
        item.setUsername(entry.getName());
        item.setDisplayName(entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()));
        item.setGamemode(entry.getGameMode());
        item.setListed(entry.isListed());
        item.setPing(entry.getLatency());
        if (entry.getSkin() != null) {
            item.setProperties(new Property[]{new Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature())});
        } else {
            item.setProperties(new Property[0]);
        }
        item.setListOrder(entry.getListOrder());
        item.setShowHat(entry.isShowHat());
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
                    TabComponent expectedDisplayName = getExpectedDisplayNames().get(item.getUuid());
                    if (expectedDisplayName != null) item.setDisplayName(toComponent(expectedDisplayName));
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
                    TabComponent expectedDisplayName = getExpectedDisplayNames().get(item.getUuid());
                    if (expectedDisplayName != null) item.setDisplayName(toComponent(expectedDisplayName));
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

    @NotNull
    protected BaseComponent toComponent(@NonNull TabComponent component) {
        return player.getPlatform().transformComponent(component, player.getVersion());
    }
}
