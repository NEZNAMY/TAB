package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.loader.Loader;
import me.neznamy.tab.platforms.fabric.loader.Loader_Latest;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class managing cross-version code in shared module.
 */
public class FabricMultiVersion {

    /** Server version */
    private static final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(FabricTAB.minecraftVersion);

    /** Method loader using latest supported MC version */
    private static final Loader loaderLatest = new Loader_Latest();

    /** Method loader using 1.20.3 - 1.21.1 */
    private static final Loader loader1_20_3 = createLoader("1_20_3");

    /** Method loader using 1.17 - 1.18.2 */
    private static final Loader loader1_18_2 = createLoader("1_18_2");

    /** Method loader using 1.14.4 */
    private static final Loader loader1_14_4 = createLoader("1_14_4");

    @SneakyThrows
    private static Loader createLoader(@NotNull String version) {
        return (Loader) Class.forName("me.neznamy.tab.platforms.fabric.loader.Loader_" + version)
                .getConstructor(ProtocolVersion.class).newInstance(serverVersion);
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
        if (serverVersion.getMinorVersion() >= 16) return loaderLatest.getLevelName(level);
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
        if (serverVersion.getMinorVersion() >= 16) loaderLatest.addSibling(parent, child);
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
        if (serverVersion.getMinorVersion() >= 16) return loaderLatest.convertModifier(modifier, modern);
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
        if (serverVersion.getMinorVersion() >= 17) return loaderLatest.newHeaderFooter(header, footer);
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
        if (serverVersion.getMinorVersion() >= 17) loaderLatest.checkTeamPacket(packet, scoreboard);
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
        if (serverVersion.getMinorVersion() >= 17) return loaderLatest.registerTeam(team);
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
        if (serverVersion.getMinorVersion() >= 17) return loaderLatest.unregisterTeam(team);
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
        if (serverVersion.getMinorVersion() >= 17) return loaderLatest.updateTeam(team);
        return loader1_14_4.updateTeam(team);
    }

    /**
     * Logs console message as info.
     *
     * @param   message
     *          Message to log
     */
    public static void logInfo(@NotNull TabComponent message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) loaderLatest.logInfo(message);
        else loader1_14_4.logInfo(message);
    }

    /**
     * Logs console message as warn.
     *
     * @param   message
     *          Message to log
     */
    public static void logWarn(@NotNull TabComponent message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_18_2.getNetworkId()) loaderLatest.logWarn(message);
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
        if (serverVersion.getMinorVersion() >= 19) return loaderLatest.newTextComponent(text);
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
        if (serverVersion.getMinorVersion() >= 19) loaderLatest.sendMessage(source, message);
        else loader1_14_4.sendMessage(source, message);
    }

    /**
     * Sets style in a component to specified style.
     *
     * @param   component
     *          Component to change style of
     * @param   style
     *          Style to use
     */
    public static void setStyle(@NotNull Component component, @NotNull Style style) {
        if (serverVersion.getMinorVersion() >= 19) loaderLatest.setStyle(component, style);
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
    public static void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) loaderLatest.sendMessage(player, message);
        else if (serverVersion.getMinorVersion() >= 19) loader1_20_3.sendMessage(player, message);
        else if (serverVersion.getMinorVersion() >= 16) loader1_18_2.sendMessage(player, message);
        else loader1_14_4.sendMessage(player, message);
    }

    /**
     * Returns {@code true} if packet is player info packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is player info packet, {@code false} if not
     */
    public static boolean isPlayerInfo(@NotNull Packet<?> packet) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loaderLatest.isPlayerInfo(packet);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) loaderLatest.onPlayerInfo(receiver, packet);
        else if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) loader1_20_3.onPlayerInfo(receiver, packet);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) return loaderLatest.buildTabListPacket(action, builder);
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) return loader1_20_3.buildTabListPacket(action, builder);
        if (serverVersion.getMinorVersion() >= 17) return loader1_18_2.buildTabListPacket(action, builder);
        return loader1_14_4.buildTabListPacket(action, builder);
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
        if (serverVersion.getMinorVersion() >= 20) return loaderLatest.getLevel(player);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderLatest.propertyToSkin(property);
        return loader1_14_4.propertyToSkin(property);
    }

    /**
     * Returns player's ping.
     *
     * @param   player
     *          Player to get ping of
     * @return  Player's ping
     */
    public static int getPing(@NotNull ServerPlayer player) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderLatest.getPing(player);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderLatest.getDisplaySlot(packet);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderLatest.setDisplaySlot(slot, objective);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) return loaderLatest.getChannel(player);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderLatest.getMSPT(server);
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
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderLatest.removeScore(objective, holder);
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
                                         @NotNull RenderType renderType, @Nullable TabComponent numberFormat) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loaderLatest.newObjective(name, displayName, renderType, numberFormat);
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
                                     @Nullable Component displayName, @Nullable TabComponent numberFormat) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_5.getNetworkId()) return loaderLatest.setScore(objective, holder, score, displayName, numberFormat);
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) return loader1_20_3.setScore(objective, holder, score, displayName, numberFormat);
        return loader1_14_4.setScore(objective, holder, score, displayName, numberFormat);
    }

    /**
     * Creates command source stack from ServerPlayer.
     *
     * @param   player
     *          Player to create command source stack from
     * @return  command source stack from player
     */
    @NotNull
    public static CommandSourceStack createCommandSourceStack(@NotNull ServerPlayer player) {
        if (serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) return loaderLatest.createCommandSourceStack(player);
        return loader1_14_4.createCommandSourceStack(player);
    }
}
