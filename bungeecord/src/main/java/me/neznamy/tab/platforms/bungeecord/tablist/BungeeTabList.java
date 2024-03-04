package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Abstract TabList class for BungeeCord containing
 * common code for all implementations.
 */
public abstract class BungeeTabList implements TabList {

    /** Player this TabList belongs to */
    @NonNull
    protected final BungeeTabPlayer player;

    /** Pointer to UUIDs in player's TabList */
    private final Collection<UUID> uuids;

    @Setter
    @Getter
    protected boolean antiOverride;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabPlayer, BaseComponent> expectedDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    private final RedisSupport redisSupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<RedisPlayer, BaseComponent> expectedRedisDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected BungeeTabList(@NonNull BungeeTabPlayer player) {
        this.player = player;
        uuids = (Collection<UUID>) ReflectionUtils.getField(ServerUnique.class, "uuids").get(((UserConnection)player.getPlayer()).getTabListHandler());
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
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
    @NonNull
    public Item item(@NonNull UUID id) {
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
    @NonNull
    public Item entryToItem(Entry entry) {
        Item item = item(entry.getUniqueId());
        if (entry.getDisplayName() != null) {
            item.setDisplayName(player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion()));
        }
        setExpectedDisplayName(entry.getUniqueId(), item.getDisplayName());
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
                    if (antiOverride) {
                        BaseComponent expectedDisplayName = getExpectedDisplayName(item.getUuid());
                        if (expectedDisplayName != null) item.setDisplayName(expectedDisplayName);
                    }
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
                    if (antiOverride) {
                        BaseComponent expectedDisplayName = getExpectedDisplayName(item.getUuid());
                        if (expectedDisplayName != null) item.setDisplayName(expectedDisplayName);
                    }
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

    @Nullable
    public BaseComponent getExpectedDisplayName(@NotNull UUID id) {
        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(id);
        if (player != null && expectedDisplayNames.containsKey(player)) {
            return expectedDisplayNames.get(player);
        }
        if (redisSupport != null) {
            RedisPlayer redisPlayer = redisSupport.getRedisPlayers().get(id);
            if (redisPlayer != null && expectedRedisDisplayNames.containsKey(redisPlayer)) {
                return expectedRedisDisplayNames.get(redisPlayer);
            }
        }
        return null;
    }

    public void setExpectedDisplayName(@NotNull UUID id, @Nullable BaseComponent displayName) {
        if (!antiOverride) return;
        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(id);
        if (player != null) expectedDisplayNames.put(player, displayName);

        if (redisSupport != null) {
            RedisPlayer redisPlayer = redisSupport.getRedisPlayers().get(id);
            if (redisPlayer != null) expectedRedisDisplayNames.put(redisPlayer, displayName);
        }
    }
}
