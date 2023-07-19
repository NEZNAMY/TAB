package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class FabricTabPlayer extends BackendTabPlayer {

    private static final Entity dummyEntity = new ArmorStand(null, 0, 0, 0);

    private final Scoreboard<FabricTabPlayer> scoreboard = new FabricScoreboard(this);
    private final TabList tabList = new FabricTabList(this);
    private final BossBar bossBar = new FabricBossBar(this);

    public FabricTabPlayer(ServerPlayer player) {
        super(player, player.getUUID(), player.getGameProfile().getName(), "N/A");
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return FabricTAB.getInstance().hasPermission(getPlayer().createCommandSourceStack(), permission);
    }

    @Override
    public int getPing() {
        return getPlayer().latency;
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendSystemMessage(FabricTAB.getInstance().toComponent(message, getVersion()));
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
    public @Nullable TabList.Skin getSkin() {
        Collection<Property> properties = getPlayer().getGameProfile().getProperties().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null;
        Property skinProperty = properties.iterator().next();
        return new TabList.Skin(skinProperty.getValue(), skinProperty.getSignature());
    }

    @Override
    public @NotNull ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    public void sendPacket(Packet<?> packet) {
        getPlayer().connection.send(packet);
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().gameMode.getGameModeForPlayer().getId();
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    public String getDisplayName() {
        return getPlayer().getDisplayName().getString(); // Will make it work properly if someone asks
    }

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data) {
        getPlayer().connection.send(new ClientboundAddEntityPacket(entityId, id,
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                (EntityType<?>) entityType, 0, Vec3.ZERO, 0));
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        getPlayer().connection.send(new ClientboundSetEntityDataPacket(entityId, Objects.requireNonNull(((SynchedEntityData) data.build()).packDirty())));
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        dummyEntity.setId(entityId);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        dummyEntity.setYRot(location.getYaw());
        dummyEntity.setXRot(location.getPitch());
        getPlayer().connection.send(new ClientboundTeleportEntityPacket(dummyEntity));
    }

    @Override
    public void destroyEntities(int... entities) {
        getPlayer().connection.send(new ClientboundRemoveEntitiesPacket(entities));
    }
}
