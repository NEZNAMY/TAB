package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.loader.Loader;
import me.neznamy.tab.platforms.fabric.loader.Loader_1_21;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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

    /** Server version */
    private static final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(FabricTAB.minecraftVersion);

    /** Method loader using latest supported MC version */
    private static final Loader loaderNew = new Loader_1_21();

    /** Method loader using 1.18.2 */
    private static final Loader loader1_18_2 = createLoader("1_18_2");

    /** Method loader using 1.14.4 */
    private static final Loader loader1_14_4 = createLoader("1_14_4");

    @SneakyThrows
    private static Loader createLoader(@NotNull String version) {
        return (Loader) Class.forName("me.neznamy.tab.platforms.fabric.loader.Loader_" + version)
                .getConstructor(ProtocolVersion.class).newInstance(serverVersion);
    }

    /**
     * Returns {@code true} if player is sneaking, {@code false} if not.
     *
     * @param   player
     *          Player to check sneak status of
     * @return  {@code true} if player is sneaking, {@code false} if not
     */
    public static boolean isSneaking(@NotNull ServerPlayer player) {
        if (serverVersion.getMinorVersion() >= 15) return loaderNew.isSneaking(player);
        return loader1_14_4.isSneaking(player);
    }

    /**
     * Returns name of specified world.
     *
     * @param   level
     *          World to get name of
     * @return  Name of the world
     */
    @NotNull
    public static String getLevelName(@NotNull Level level) {
        if (serverVersion.getMinorVersion() >= 16) return loaderNew.getLevelName(level);
        return loader1_14_4.getLevelName(level);
    }

    /**
     * Adds sibling to a component.
     *
     * @param   parent
     *          Parent to add sibling to
     * @param   child
     *          Sibling to add
     */
    public static void addSibling(@NotNull Component parent, @NotNull Component child) {
        if (serverVersion.getMinorVersion() >= 16) loaderNew.addSibling(parent, child);
        else loader1_14_4.addSibling(parent, child);
    }

    /**
     * Converts TAB's ChatModifier class to Minecraft Style class.
     *
     * @param   modifier
     *          Modifier to convert
     * @param   modern
     *          Whether RGB should be supported or not
     * @return  Converted style
     */
    @NotNull
    public static Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        if (serverVersion.getMinorVersion() >= 16) return loaderNew.convertModifier(modifier, modern);
        return loader1_14_4.convertModifier(modifier, modern);
    }

    /**
     * Creates new Header/Footer packet with given parameters.
     *
     * @param   header
     *          Header to use
     * @param   footer
     *          Footer to use
     * @return  Packet with given parameters
     */
    @NotNull
    public static Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.newHeaderFooter(header, footer);
        return loader1_14_4.newHeaderFooter(header, footer);
    }

    /**
     * Checks outgoing team packet in pipeline to potentially remove players from it.
     *
     * @param   packet
     *          Packet to check
     * @param   scoreboard
     *          Scoreboard of player who received the packet
     */
    public static void checkTeamPacket(@NotNull Packet<?> packet, @NotNull FabricScoreboard scoreboard) {
        if (serverVersion.getMinorVersion() >= 17) loaderNew.checkTeamPacket(packet, scoreboard);
        else loader1_14_4.checkTeamPacket(packet, scoreboard);
    }

    /**
     * Creates team register packet using given team.
     *
     * @param   team
     *          Team to register
     * @return  Team register packet using given team
     */
    @NotNull
    public static Packet<?> registerTeam(@NotNull PlayerTeam team) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.registerTeam(team);
        return loader1_14_4.registerTeam(team);
    }

    /**
     * Creates team unregister packet using given team.
     *
     * @param   team
     *          Team to unregister
     * @return  Team unregister packet using given team
     */
    @NotNull
    public static Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.unregisterTeam(team);
        return loader1_14_4.unregisterTeam(team);
    }

    /**
     * Creates team update packet using given team.
     *
     * @param   team
     *          Team to update
     * @return  Team update packet using given team
     */
    @NotNull
    public static Packet<?> updateTeam(@NotNull PlayerTeam team) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.updateTeam(team);
        return loader1_14_4.updateTeam(team);
    }

    /**
     * Returns destroyed entities from destroy entity packet.
     *
     * @param   destroyPacket
     *          Entity destroy packet
     * @return  Destroyed entities
     */
    public static int[] getDestroyedEntities(@NotNull Packet<?> destroyPacket) {
        if (serverVersion.getMinorVersion() >= 17) return loaderNew.getDestroyedEntities(destroyPacket);
        return loader1_14_4.getDestroyedEntities(destroyPacket);
    }

    /**
     * Logs console message as info.
     *
     * @param   message
     *          Message to log
     */
    public static void logInfo(@NotNull TabComponent message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) loaderNew.logInfo(message);
        else loader1_14_4.logInfo(message);
    }

    /**
     * Logs console message as warn.
     *
     * @param   message
     *          Message to log
     */
    public static void logWarn(@NotNull TabComponent message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) loaderNew.logWarn(message);
        else loader1_14_4.logWarn(message);
    }

    /**
     * Creates new text component using given text.
     *
     * @param   text
     *          Component text
     * @return  Text component with given text
     */
    @NotNull
    public static Component newTextComponent(@NotNull String text) {
        if (serverVersion.getMinorVersion() >= 19) return loaderNew.newTextComponent(text);
        return loader1_14_4.newTextComponent(text);
    }

    /**
     * Sends message to command source.
     *
     * @param   source
     *          Command source to send message to
     * @param   message
     *          Message to send
     */
    public static void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        if (serverVersion.getMinorVersion() >= 19) loaderNew.sendMessage(source, message);
        else loader1_14_4.sendMessage(source, message);
    }

    /**
     * Creates spawn entity packet with given parameters.
     *
     * @param   level
     *          World to fill for dummy entity
     * @param   id
     *          Entity ID
     * @param   uuid
     *          Entity UUID
     * @param   type
     *          Entity type
     * @param   location
     *          Spawn location
     * @return  Spawn entity packet with given parameters
     */
    @NotNull
    public static Packet<ClientGamePacketListener> spawnEntity(@NotNull Level level, int id, @NotNull UUID uuid, @NotNull Object type, @NotNull Location location) {
        if (serverVersion.getMinorVersion() >= 19) return loaderNew.spawnEntity(level, id, uuid, type, location);
        return loader1_14_4.spawnEntity(level, id, uuid, type, location);
    }

    /**
     * Sets style in a component to specified style.
     *
     * @param   component
     *          Component to change style of
     * @param   style
     *          Style to use
     */
    @SneakyThrows
    public static void setStyle(@NotNull Component component, @NotNull Style style) {
        if (serverVersion.getMinorVersion() >= 19) loaderNew.setStyle(component, style);
        else if (serverVersion.getMinorVersion() >= 16) loader1_18_2.setStyle(component, style);
        else loader1_14_4.setStyle(component, style);
    }

    /**
     * Sends message to player.
     *
     * @param   player
     *          Player to send message to
     * @param   message
     *          Message to send
     */
    @SneakyThrows
    public static void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        if (serverVersion.getMinorVersion() >= 19) loaderNew.sendMessage(player, message);
        else if (serverVersion.getMinorVersion() >= 16) loader1_18_2.sendMessage(player, message);
        else loader1_14_4.sendMessage(player, message);
    }

    /**
     * Creates entity metadata packet with given metadata.
     *
     * @param   entityId
     *          Entity ID to change metadata of
     * @param   data
     *          Metadata to change
     * @return  Entity metadata packet with given parameters
     */
    @NotNull
    public static Packet<ClientGamePacketListener> newEntityMetadata(int entityId, @NotNull EntityData data) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.newEntityMetadata(entityId, data);
        return loader1_14_4.newEntityMetadata(entityId, data);
    }

    /**
     * Creates entity data with given parameters.
     *
     * @param   viewer
     *          Viewer of custom name
     * @param   flags
     *          Entity flags
     * @param   displayName
     *          Custom name
     * @param   nameVisible
     *          Custom name visibility
     * @return  Entity data with given parameters
     */
    @NotNull
    public static EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible) {
        int position = EntityData.getArmorStandFlagsPosition(serverVersion.getMinorVersion());
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.createDataWatcher(viewer, flags, displayName, nameVisible, position);
        return loader1_14_4.createDataWatcher(viewer, flags, displayName, nameVisible, position);
    }

    /**
     * Returns {@code true} if packet is player info packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is player info packet, {@code false} if not
     */
    public static boolean isPlayerInfo(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.isPlayerInfo(packet);
        return loader1_14_4.isPlayerInfo(packet);
    }

    /**
     * Processed player info packet for anti-override and similar.
     *
     * @param   receiver
     *          Player who received the packet
     * @param   packet
     *          Received packet
     */
    public static void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) loaderNew.onPlayerInfo(receiver, packet);
        else if (serverVersion.getMinorVersion() >= 17) loader1_18_2.onPlayerInfo(receiver, packet);
        else loader1_14_4.onPlayerInfo(receiver, packet);
    }

    /**
     * Creates tablist entry packet using given parameters.
     *
     * @param   action
     *          Tablist action
     * @param   builder
     *          Entry data
     * @return  Tablist entry packet with given parameters
     */
    @NotNull
    public static Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder builder) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderNew.buildTabListPacket(action, builder);
        else if (serverVersion.getMinorVersion() >= 17) return loader1_18_2.buildTabListPacket(action, builder);
        return loader1_14_4.buildTabListPacket(action, builder);
    }

    /**
     * Returns {@code true} if packet is bundle packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is bundle packet, {@code false} if not
     */
    public static boolean isBundlePacket(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) return loaderNew.isBundlePacket(packet);
        return loader1_14_4.isBundlePacket(packet);
    }

    /**
     * Returns packets bundled in given bundle packet.
     *
     * @param   bundlePacket
     *          Bundle packet
     * @return  Bundled packets
     */
    @NotNull
    public static Iterable<Object> getBundledPackets(@NotNull Packet<?> bundlePacket) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) return loaderNew.getBundledPackets(bundlePacket);
        return loader1_14_4.getBundledPackets(bundlePacket);
    }

    /**
     * Sends packets to player as a bundle.
     *
     * @param   player
     *          Player to send packets to
     * @param   packets
     *          Packets to send
     */
    public static void sendPackets(@NotNull ServerPlayer player, @NotNull Iterable<Packet<ClientGamePacketListener>> packets) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_4.getNetworkId()) loaderNew.sendPackets(player, packets);
        else loader1_14_4.sendPackets(player, packets);
    }

    /**
     * Returns player's world
     *
     * @param   player
     *          Player to get world of
     * @return  Player's world
     */
    @NotNull
    public static Level getLevel(@NotNull ServerPlayer player) {
        if (serverVersion.getMinorVersion() >= 20) return loaderNew.getLevel(player);
        return loader1_14_4.getLevel(player);
    }

    /**
     * Converts minecraft property class into TAB skin class.
     *
     * @param   property
     *          Property to convert
     * @return  Converted skin
     */
    @NotNull
    public static TabList.Skin propertyToSkin(@NotNull Property property) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.propertyToSkin(property);
        return loader1_14_4.propertyToSkin(property);
    }

    /**
     * Returns {@code true} if packet is player spawn packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is player spawn packet, {@code false} if not
     */
    public static boolean isSpawnPlayerPacket(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.isSpawnPlayerPacket(packet);
        return loader1_14_4.isSpawnPlayerPacket(packet);
    }

    /**
     * Returns player ID of given player spawn packet.
     *
     * @param   packet
     *          Player spawn packet
     * @return  Player ID
     */
    public static int getSpawnedPlayerId(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getSpawnedPlayerId(packet);
        return loader1_14_4.getSpawnedPlayerId(packet);
    }

    /**
     * Returns player's ping.
     *
     * @param   player
     *          Player to get ping of
     * @return  Player's ping
     */
    public static int getPing(@NotNull ServerPlayer player) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getPing(player);
        return loader1_14_4.getPing(player);
    }

    /**
     * Returns display slot of given display objective packet.
     *
     * @param   packet
     *          Display objective packet
     * @return  Display slot of packet
     */
    public static int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getDisplaySlot(packet);
        return loader1_14_4.getDisplaySlot(packet);
    }

    /**
     * Creates display objective packet with given parameters.
     *
     * @param   slot
     *          Display slot
     * @param   objective
     *          Objective to display
     * @return  Display objective packet with given parameters
     */
    @NotNull
    public static Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.setDisplaySlot(slot, objective);
        return loader1_14_4.setDisplaySlot(slot, objective);
    }

    /**
     * Returns player's network channel.
     *
     * @param   player
     *          Player to get channel of
     * @return  Player's channel
     */
    @NotNull
    public static Channel getChannel(@NotNull ServerPlayer player) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderNew.getChannel(player);
        return loader1_14_4.getChannel(player);
    }

    /**
     * Returns server's current milliseconds per tick.
     *
     * @param   server
     *          Server to get MSPT value from
     * @return  Server's milliseconds per tick
     */
    public static float getMSPT(@NotNull MinecraftServer server) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.getMSPT(server);
        return loader1_14_4.getMSPT(server);
    }

    /**
     * Creates new remove score packet with given parameters.
     *
     * @param   objective
     *          Objective to remove score from
     * @param   holder
     *          Score holder to remove
     * @return  Remove score packet with given parameters
     */
    @NotNull
    public static Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.removeScore(objective, holder);
        return loader1_14_4.removeScore(objective, holder);
    }

    /**
     * Creates new objective with given parameters.
     *
     * @param   name
     *          Objective name
     * @param   displayName
     *          Objective display name
     * @param   renderType
     *          Score render type
     * @param   numberFormat
     *          Score number format (1.20.3+)
     * @return  New objective with given parameters
     */
    @NotNull
    public static Objective newObjective(@NotNull String name, @NotNull Component displayName,
                                         @NotNull RenderType renderType, @Nullable Component numberFormat) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.newObjective(name, displayName, renderType, numberFormat);
        return loader1_14_4.newObjective(name, displayName, renderType, numberFormat);
    }

    /**
     * Creates a new set score packet with given parameters.
     *
     * @param   objective
     *          Objective to set score in
     * @param   holder
     *          Score holder
     * @param   score
     *          Score value
     * @param   displayName
     *          Display name of score holder (1.20.3+)
     * @param   numberFormat
     *          Number format of score value (1.20.3+)
     * @return  New set score packet with given parameters
     */
    @NotNull
    public static Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score,
                                     @Nullable Component displayName, @Nullable Component numberFormat) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderNew.setScore(objective, holder, score, displayName, numberFormat);
        return loader1_14_4.setScore(objective, holder, score, displayName, numberFormat);
    }
}
