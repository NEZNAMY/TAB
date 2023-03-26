package me.neznamy.tab.platforms.krypton.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.feature.PacketSendListener;
import me.neznamy.tab.platforms.krypton.Main;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.kryptonmc.api.entity.Entity;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.api.event.EventNode;
import org.kryptonmc.api.util.Position;
import org.kryptonmc.krypton.entity.KryptonEntityTypes;
import org.kryptonmc.krypton.entity.metadata.MetadataHolder;
import org.kryptonmc.krypton.entity.metadata.MetadataKeys;
import org.kryptonmc.krypton.entity.player.KryptonPlayer;
import org.kryptonmc.krypton.packet.EntityPacket;
import org.kryptonmc.krypton.packet.MovementPacket;
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities;
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnPlayer;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KryptonNameTagX extends BackendNameTagX implements PacketSendListener {

    private final Main plugin;

    private final EventListener eventListener = new EventListener(this);

    @Override
    public void load() {
        EventNode<?> eventNode = EventNode.all("tab_nametagx");
        plugin.getEventNode().addChild(eventNode);
        eventNode.registerListeners(eventListener);
        super.load();
    }

    @Override
    public void onPacketSend(TabPlayer receiver, Object packet) {
        if (receiver.getVersion().getMinorVersion() < 8) return;
        if (!receiver.isLoaded() || isPlayerDisabled(receiver) || getDisabledUnlimitedPlayers().contains(receiver)) return;
        if (packet instanceof  MovementPacket) {
            if (!(packet instanceof EntityPacket)) return;
            packetListener.onEntityMove((BackendTabPlayer) receiver, ((EntityPacket) packet).getEntityId());
        } else if (packet instanceof PacketOutSpawnPlayer){
            packetListener.onEntitySpawn((BackendTabPlayer) receiver, ((PacketOutSpawnPlayer) packet).entityId());
        } else if (packet instanceof PacketOutRemoveEntities) {
            packetListener.onEntityDestroy((BackendTabPlayer) receiver, ((PacketOutRemoveEntities) packet).ids());
        }
    }

    @Override
    public boolean isOnBoat(TabPlayer player) {
        return getVehicleManager().isOnBoat(player);
    }

    @Override
    public double getDistance(TabPlayer player1, TabPlayer player2) {
        Position pos1 = ((Player)player1.getPlayer()).getPosition();
        Position pos2 = ((Player)player2.getPlayer()).getPosition();
        return Math.sqrt((pos1.x() - pos2.x()) * (pos1.x() - pos2.x()) + (pos1.z() - pos2.z()) * (pos1.z() - pos2.z()));
    }

    @Override
    public boolean areInSameWorld(TabPlayer player1, TabPlayer player2) {
        return ((Player)player1.getPlayer()).getWorld() != ((Player)player2.getPlayer()).getWorld();
    }

    @Override
    public boolean canSee(TabPlayer viewer, TabPlayer target) {
        return true;
    }

    @Override
    public void unregisterListener() {
        this.plugin.getEventNode().unregisterListeners(this.eventListener);
    }
    
    @Override
    public List<Integer> getPassengers(Object vehicle) {
        return ((Entity)vehicle).getPassengers().stream().map(Entity::getId).collect(Collectors.toList());
    }

    @Override
    public void registerVehiclePlaceholder() {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%vehicle%", 100, p -> String.valueOf(((Player)p.getPlayer()).getVehicle()));
    }

    @Override
    public Object getVehicle(TabPlayer player) {
        return ((Player)player.getPlayer()).getVehicle();
    }

    @Override
    public int getEntityId(Object entity) {
        return ((Entity)entity).getId();
    }

    @Override
    public String getEntityType(Object entity) {
        return ((Entity)entity).getType().key().value();
    }

    @Override
    public boolean isSneaking(TabPlayer player) {
        return ((Player)player.getPlayer()).isSneaking();
    }

    @Override
    public boolean isSwimming(TabPlayer player) {
        return ((Player)player.getPlayer()).isSwimming();
    }

    @Override
    public boolean isGliding(TabPlayer player) {
        return ((Player)player.getPlayer()).isGliding();
    }

    @Override
    public boolean isSleeping( TabPlayer player) {
        return false;
    }

    @Override
    public Object getArmorStandType() {
        return KryptonEntityTypes.ARMOR_STAND;
    }

    @Override
    public double getX(TabPlayer player) {
        return ((Player)player.getPlayer()).getPosition().x();
    }

    @Override
    public double getY(Object entity) {
        return ((Entity)entity).getPosition().y();
    }

    @Override
    public double getZ(TabPlayer player) {
        return ((Player)player.getPlayer()).getPosition().z();
    }

    @Override
    public EntityData createDataWatcher(TabPlayer viewer, byte flags, String displayName, boolean nameVisible) {
        KryptonPlayer viewerPlayer = (KryptonPlayer) viewer.getPlayer();
        MetadataHolder holder = new MetadataHolder(viewerPlayer);
        holder.define(MetadataKeys.Entity.FLAGS, flags);
        holder.define(MetadataKeys.Entity.CUSTOM_NAME, GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(displayName).toString(viewer.getVersion())));
        holder.define(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, nameVisible);
        holder.define(MetadataKeys.ArmorStand.FLAGS, (byte) 16);
        return new WrappedEntityData(holder);
    }
}
