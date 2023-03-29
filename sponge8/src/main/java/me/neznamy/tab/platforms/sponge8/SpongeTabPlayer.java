package me.neznamy.tab.platforms.sponge8;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SpongeTabPlayer extends BackendTabPlayer {

    private static final ArmorStand dummyEntity = new ArmorStand(EntityType.ARMOR_STAND, null);

    @Getter private final Scoreboard scoreboard = new SpongeScoreboard(this);
    @Getter private final TabList tabList = new SpongeTabList(this);
    @Getter private final BossBarHandler bossBarHandler = new SpongeBossBarHandler(this);

    public SpongeTabPlayer(ServerPlayer player) {
        super(player, player.uniqueId(), player.name(), TAB.getInstance().getConfiguration().getServerName(),
                player.world().key().value(), getProtocolVersion(player));
    }

    private static int getProtocolVersion(ServerPlayer player) {
        if (Sponge.pluginManager().plugin(TabConstants.Plugin.VIAVERSION.toLowerCase()).isPresent()) {
            return ProtocolVersion.getPlayerVersionVia(player.uniqueId(), player.name());
        }
        return TAB.getInstance().getServerVersion().getNetworkId();
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().connection().latency();
    }

    @Override
    public void sendPacket(Object packet) {
        if (packet == null) return;
        ((net.minecraft.server.level.ServerPlayer) getPlayer()).connection.send((Packet<?>) packet);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(Sponge8TAB.getAdventureCache().get(message, getVersion()));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        for (PotionEffect effect : getPlayer().get(Keys.POTION_EFFECTS).orElse(Collections.emptyList())) {
            if (effect.type() == PotionEffectTypes.INVISIBILITY.get()) return true;
        }
        return false;
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public Skin getSkin() {
        List<ProfileProperty> list = getPlayer().profile().properties();
        if (list.isEmpty()) return null;
        return new Skin(list.get(0).value(), list.get(0).signature().orElse(null));
    }

    @Override
    public ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public boolean isVanished() {
        return getPlayer().vanishState().get().invisible();
    }

    @Override
    public int getGamemode() {
        if (getPlayer().gameMode().get() == GameModes.CREATIVE.get()) return 1;
        if (getPlayer().gameMode().get() == GameModes.ADVENTURE.get()) return 2;
        if (getPlayer().gameMode().get() == GameModes.SPECTATOR.get()) return 3;
        return 0;
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().tabList().setHeaderAndFooter(Sponge8TAB.getAdventureCache().get(header, version), Sponge8TAB.getAdventureCache().get(footer, version));
    }

    public void setPlayer(final ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void spawnEntity(int entityId, UUID id, Object entityType, Location location, EntityData data) {
        sendPacket(new ClientboundAddEntityPacket(entityId, id, location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(), Registry.ENTITY_TYPE.byId((Integer) entityType), 0, Vec3.ZERO));
        updateEntityMetadata(entityId, data);
    }

    @Override
    public void updateEntityMetadata(int entityId, EntityData data) {
        sendPacket(new ClientboundSetEntityDataPacket(entityId, (SynchedEntityData) data.build(), true));
    }

    @Override
    public void teleportEntity(int entityId, Location location) {
        // While the entity is shared, packets are build in a single thread, so no risk of concurrent access
        dummyEntity.setId(entityId);
        dummyEntity.setPos(location.getX(), location.getY(), location.getZ());
        dummyEntity.xRot = location.getYaw();
        dummyEntity.yRot = location.getPitch();
        sendPacket(new ClientboundTeleportEntityPacket(dummyEntity));
    }

    @Override
    public void destroyEntities(int... entities) {
        sendPacket(new ClientboundRemoveEntitiesPacket(entities));
    }
}
