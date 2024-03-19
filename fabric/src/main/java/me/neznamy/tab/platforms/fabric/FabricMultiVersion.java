package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.loader.Loader;
import me.neznamy.tab.platforms.fabric.loader.Loader_1_20_4;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Class managing cross-version code in shared module.
 */
public class FabricMultiVersion {

    public static final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(FabricTAB.minecraftVersion);
    public static final Loader loaderNew = new Loader_1_20_4();
    public static final Loader loaderOld = createOldLoader();

    @SneakyThrows
    private static Loader createOldLoader() {
        return (Loader) Class.forName("me.neznamy.tab.platforms.fabric.loader.Loader_1_14_4")
                .getConstructor(ProtocolVersion.class).newInstance(serverVersion);
    }

    public static boolean isSneaking(@NotNull ServerPlayer player) {
        if (serverVersion.getMinorVersion() >= 15) return loaderNew.isSneaking(player);
        return loaderOld.isSneaking(player);
    }

    @NotNull
    public static String getLevelName(@NotNull Level level) {
        if (serverVersion.getMinorVersion() >= 16) return loaderNew.getLevelName(level);
        return loaderOld.getLevelName(level);
    }

    public static void addSibling(@NotNull Component parent, @NotNull Component child) {
        if (serverVersion.getMinorVersion() >= 16) loaderNew.addSibling(parent, child);
        else loaderOld.addSibling(parent, child);
    }

    @NotNull
    public static Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        if (serverVersion.getMinorVersion() >= 16) return loaderNew.convertModifier(modifier, modern);
        return loaderOld.convertModifier(modifier, modern);
    }

    @NotNull
    public static Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.newHeaderFooter(header, footer);
        return loaderOld.newHeaderFooter(header, footer);
    }

    public static boolean isTeamPacket(@NotNull Packet<?> packet) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.isTeamPacket(packet);
        return loaderOld.isTeamPacket(packet);
    }

    @NotNull
    public static Packet<?> registerTeam(@NotNull PlayerTeam team) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.registerTeam(team);
        return loaderOld.registerTeam(team);
    }

    @NotNull
    public static Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.unregisterTeam(team);
        return loaderOld.unregisterTeam(team);
    }

    @NotNull
    public static Packet<?> updateTeam(@NotNull PlayerTeam team) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.updateTeam(team);
        return loaderOld.updateTeam(team);
    }

    @SneakyThrows
    public static int[] getDestroyedEntities(Packet<?> destroyPacket) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_17_1.getNetworkId()) return loaderNew.getDestroyedEntities(destroyPacket);
        if (serverVersion.getMinorVersion() >= 17) return new int[]{ReflectionUtils.getOnlyField(destroyPacket.getClass()).getInt(destroyPacket)};
        return loaderOld.getDestroyedEntities(destroyPacket);
    }

    @SneakyThrows
    public static void destroyEntities(@NotNull ServerPlayer player, int[] entities) {
        if (serverVersion == ProtocolVersion.V1_17) {
            for (int entity : entities) {
                // While the actual packet name is different, fabric-mapped name is the same
                player.connection.send(ClientboundRemoveEntitiesPacket.class.getConstructor(int.class).newInstance(entity));
            }
        } else {
            player.connection.send(new ClientboundRemoveEntitiesPacket(entities));
        }
    }

    public static void logInfo(@NotNull TabComponent message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) loaderNew.logInfo(message);
        else loaderOld.logInfo(message);
    }

    public static void logWarn(@NotNull TabComponent message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) loaderNew.logWarn(message);
        else loaderOld.logWarn(message);
    }

    @NotNull
    public static Component newTextComponent(@NotNull String text) {
        if (serverVersion.getMinorVersion() >= 19) return loaderNew.newTextComponent(text);
        return loaderOld.newTextComponent(text);
    }

    public static void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        if (serverVersion.getMinorVersion() >= 19) loaderNew.sendMessage(source, message);
        else loaderOld.sendMessage(source, message);
    }

    @NotNull
    public static Packet<ClientGamePacketListener> spawnEntity(Level level, int id, UUID uuid, Object type, Location location) {
        if (serverVersion.getMinorVersion() >= 19) return loaderNew.spawnEntity(level, id, uuid, type, location);
        return loaderOld.spawnEntity(level, id, uuid, type, location);
    }

    @SneakyThrows
    public static void setStyle(@NotNull Component component, @NotNull Style style) {
        if (serverVersion.getMinorVersion() >= 19) loaderNew.setStyle(component, style);
        else if (serverVersion.getMinorVersion() >= 16) component.getClass().getMethod("method_10862", Style.class).invoke(component, style);
        else loaderOld.setStyle(component, style);
    }

    @SneakyThrows
    public static void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        if (serverVersion.getMinorVersion() >= 19) loaderNew.sendMessage(player, message);
        else if (serverVersion.getMinorVersion() >= 16)
                player.getClass().getMethod("method_9203", Component.class, UUID.class).invoke(player, message, new UUID(0, 0));
        else loaderOld.sendMessage(player, message);
    }

    @NotNull
    public static Packet<ClientGamePacketListener> newEntityMetadata(int entityId, @NotNull EntityData data) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.newEntityMetadata(entityId, data);
        return loaderOld.newEntityMetadata(entityId, data);
    }

    @NotNull
    public static EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible) {
        int position = EntityData.getArmorStandFlagsPosition(serverVersion.getMinorVersion());
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.createDataWatcher(viewer, flags, displayName, nameVisible, position);
        return loaderOld.createDataWatcher(viewer, flags, displayName, nameVisible, position);
    }

    public static boolean isPlayerInfo(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.isPlayerInfo(packet);
        return loaderOld.isPlayerInfo(packet);
    }

    public static void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) loaderNew.onPlayerInfo(receiver, packet);
        else loaderOld.onPlayerInfo(receiver, packet);
    }

    @NotNull
    public static Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder builder) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.buildTabListPacket(action, builder);
        return loaderOld.buildTabListPacket(action, builder);
    }

    public static boolean isBundlePacket(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) return loaderNew.isBundlePacket(packet);
        return loaderOld.isBundlePacket(packet);
    }

    @NotNull
    public static Iterable<Object> getBundledPackets(@NotNull Packet<?> bundlePacket) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) return loaderNew.getBundledPackets(bundlePacket);
        return loaderOld.getBundledPackets(bundlePacket);
    }

    public static void sendPackets(@NotNull ServerPlayer player, @NotNull Iterable<Packet<ClientGamePacketListener>> packets) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) loaderNew.sendPackets(player, packets);
        else loaderOld.sendPackets(player, packets);
    }

    @NotNull
    public static Level getLevel(@NotNull ServerPlayer player) {
        if (serverVersion.getMinorVersion() >= 20) return loaderNew.getLevel(player);
        return loaderOld.getLevel(player);
    }

    @NotNull
    public static TabList.Skin propertyToSkin(@NotNull Property property) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.propertyToSkin(property);
        return loaderOld.propertyToSkin(property);
    }

    public static boolean isSpawnPlayerPacket(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.isSpawnPlayerPacket(packet);
        return loaderOld.isSpawnPlayerPacket(packet);
    }

    public static int getSpawnedPlayerId(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getSpawnedPlayerId(packet);
        return loaderOld.getSpawnedPlayerId(packet);
    }

    public static int getPing(@NotNull ServerPlayer player) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getPing(player);
        return loaderOld.getPing(player);
    }

    public static int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getDisplaySlot(packet);
        return loaderOld.getDisplaySlot(packet);
    }

    @NotNull
    public static Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.setDisplaySlot(slot, objective);
        return loaderOld.setDisplaySlot(slot, objective);
    }

    @NotNull
    public static Channel getChannel(@NotNull ServerPlayer player) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getChannel(player);
        return loaderOld.getChannel(player);
    }

    public static float getMSPT(@NotNull MinecraftServer server) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.getMSPT(server);
        return loaderOld.getMSPT(server);
    }

    @NotNull
    public static Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.removeScore(objective, holder);
        return loaderOld.removeScore(objective, holder);
    }

    @NotNull
    public static Objective newObjective(@NotNull String name, @NotNull Component displayName,
                                         @NotNull RenderType renderType, @Nullable Component numberFormat) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.newObjective(name, displayName, renderType, numberFormat);
        return loaderOld.newObjective(name, displayName, renderType, numberFormat);
    }

    @NotNull
    public static Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score,
                                     @Nullable Component displayName, @Nullable Component numberFormat) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.setScore(objective, holder, score, displayName, numberFormat);
        return loaderOld.setScore(objective, holder, score, displayName, numberFormat);
    }
}
