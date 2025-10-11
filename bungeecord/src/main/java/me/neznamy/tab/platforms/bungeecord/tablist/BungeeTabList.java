package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.data.Property;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.tab.ServerUnique;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().setTabHeader(toComponent(header), toComponent(footer));
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
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof PlayerListHeaderFooter) {
            PlayerListHeaderFooter tablist = (PlayerListHeaderFooter) packet;
            if (header == null || footer == null) return packet;
            BaseComponent headerComponent = player.getPlatform().transformComponent(header, player.getVersion());
            BaseComponent footerComponent = player.getPlatform().transformComponent(footer, player.getVersion());
            if (tablist.getHeader() != headerComponent || tablist.getFooter() != footerComponent) {
                tablist.setHeader(headerComponent);
                tablist.setFooter(footerComponent);
            }
        }
        if (packet instanceof PlayerListItem) {
            PlayerListItem listItem = (PlayerListItem) packet;
            for (PlayerListItem.Item item : listItem.getItems()) {
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_DISPLAY_NAME || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    TabComponent forcedDisplayName = getForcedDisplayNames().get(item.getUuid());
                    if (forcedDisplayName != null) item.setDisplayName(toComponent(forcedDisplayName));
                }
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    Integer forcedGameMode = getForcedGameModes().get(item.getUuid());
                    if (forcedGameMode != null) item.setGamemode(forcedGameMode);
                }
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_LATENCY || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    if (getForcedLatency() != null) {
                        item.setPing(getForcedLatency());
                    }
                }
                if (listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, item.getUuid(), item.getUsername());
                }
            }
        } else if (packet instanceof PlayerListItemUpdate) {
            PlayerListItemUpdate update = (PlayerListItemUpdate) packet;
            for (PlayerListItem.Item item : update.getItems()) {
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME)) {
                    TabComponent forcedDisplayName = getForcedDisplayNames().get(item.getUuid());
                    if (forcedDisplayName != null) item.setDisplayName(toComponent(forcedDisplayName));
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_GAMEMODE)) {
                    Integer forcedGameMode = getForcedGameModes().get(item.getUuid());
                    if (forcedGameMode != null) item.setGamemode(forcedGameMode);
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_LATENCY)) {
                    if (getForcedLatency() != null) {
                        item.setPing(getForcedLatency());
                    }
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.ADD_PLAYER)) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, item.getUuid(), item.getUsername());
                }
            }
        }
        return packet;
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return uuids.contains(entry);
    }

    @Override
    @Nullable
    public Skin getSkin() {
        LoginResult loginResult = ((InitialHandler)player.getPlayer().getPendingConnection()).getLoginProfile();
        if (loginResult == null) return null;
        Property[] properties = loginResult.getProperties();
        if (properties == null) return null; //Offline mode
        for (Property property : properties) {
            if (property.getName().equals(TEXTURES_PROPERTY)) {
                return new Skin(property.getValue(), property.getSignature());
            }
        }
        return null;
    }

    @NotNull
    protected BaseComponent toComponent(@NonNull TabComponent component) {
        return player.getPlatform().transformComponent(component, player.getVersion());
    }
}
