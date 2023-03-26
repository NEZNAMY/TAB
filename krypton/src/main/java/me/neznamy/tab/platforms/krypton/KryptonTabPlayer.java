package me.neznamy.tab.platforms.krypton;

import lombok.Getter;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.kryptonmc.api.auth.ProfileProperty;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.krypton.entity.KryptonEntityType;
import org.kryptonmc.krypton.entity.metadata.MetadataHolder;
import org.kryptonmc.krypton.entity.player.KryptonPlayer;
import org.kryptonmc.krypton.packet.Packet;
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities;
import org.kryptonmc.krypton.packet.out.play.PacketOutSetEntityMetadata;
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnEntity;
import org.kryptonmc.krypton.packet.out.play.PacketOutTeleportEntity;

import java.util.List;
import java.util.UUID;

public class KryptonTabPlayer extends BackendTabPlayer {

    @Getter private final Scoreboard scoreboard = new KryptonScoreboard(this);
    @Getter private final TabList tabList = new KryptonTabList(this);
    @Getter private final BossBarHandler bossBarHandler = new KryptonBossBarHandler(this);

    public KryptonTabPlayer(Player player, int protocolVersion) {
        super(player, player.getUuid(), player.getProfile().name(), "N/A", player.getWorld().getName(), protocolVersion);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getConnection().latency();
    }

    @Override
    public void sendPacket(Object packet) {
        getPlayer().getConnection().send((Packet) packet);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(GsonComponentSerializer.gson().deserialize(message.toString(version)));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return false;
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public Skin getSkin() {
        List<ProfileProperty> list = getPlayer().getProfile().properties();
        if (list.isEmpty()) return null;
        return new Skin(list.get(0).value(), list.get(0).signature());
    }

    @Override
    public KryptonPlayer getPlayer() {
        return (KryptonPlayer) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().getGameMode().ordinal();
    }

    @Override
    public void setPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) {
        getPlayer().sendPlayerListHeaderAndFooter(
                GsonComponentSerializer.gson().deserialize(header.toString(version)),
                GsonComponentSerializer.gson().deserialize(footer.toString(version))
        );
    }
    @Override
    public void spawnEntity(int entityId, UUID id, Object entityType, Location location, EntityData data) {
        sendPacket(
                new PacketOutSpawnEntity(entityId, id, (KryptonEntityType<?>)entityType,
                        location.getX(), location.getY(), location.getZ(), (byte)(int)(location.getYaw() / 360*256),
                        (byte)(int)(location.getPitch() / 360*256), (byte)0, 0, (short)0, (short)0, (short)0));
        updateEntityMetadata(entityId, data);
    }

    public void updateEntityMetadata(int entityId, EntityData data) {
        sendPacket(new PacketOutSetEntityMetadata(entityId, ((MetadataHolder)data.build()).collectAll()));
    }

    public void teleportEntity(int entityId, Location location) {
        sendPacket(new PacketOutTeleportEntity(entityId, location.getX(), location.getY(), location.getZ(),
                (byte)(int)(location.getYaw() / 360*256), (byte)(int)(location.getPitch() / 360*256), false));
    }

    public void destroyEntities(int... entities) {
        sendPacket(new PacketOutRemoveEntities(entities));
    }
}
