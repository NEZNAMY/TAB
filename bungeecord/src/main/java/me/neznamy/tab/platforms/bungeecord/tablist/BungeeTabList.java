package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.tab.ServerUnique;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public abstract class BungeeTabList implements TabList {

    /** Player this TabList belongs to */
    @NotNull
    protected final BungeeTabPlayer player;

    /** Pointer to UUIDs in player's TabList */
    private final Collection<UUID> uuids;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public BungeeTabList(@NotNull BungeeTabPlayer player) {
        this.player = player;
        uuids = (Collection<UUID>) ReflectionUtils.getField(ServerUnique.class, "uuids").get(((UserConnection)player.getPlayer()).getTabListHandler());
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().setTabHeader(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        );
    }

    @NotNull
    public Item item(@NotNull UUID id) {
        Item item = new Item();
        item.setUuid(id);
        return item;
    }

    @NotNull
    public Item entryToItem(Entry entry) {
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
        return item;
    }

    public void addUuid(UUID id) {
        uuids.add(id);
    }

    public void removeUuid(UUID id) {
        uuids.remove(id);
    }
}
