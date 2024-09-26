package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.fabric.FabricScoreboard;
import me.neznamy.tab.platforms.fabric.FabricTabList;
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
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for executing methods that have changed over the versions.
 * Each implementation of this interface is compiled for a different version of Minecraft.
 * Then, internal logic is used to decide which implementation to use for each method
 * based on server version.
 */
public interface Loader {

    /** Dummy scoreboard for objectives */
    Scoreboard dummyScoreboard = new Scoreboard();

    /**
     * Returns name of specified world.
     *
     * @param   level
     *          World to get name of
     * @return  Name of the world
     */
    @NotNull
    default String getLevelName(@NotNull Level level) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Converts minecraft property class into TAB skin class.
     *
     * @param   property
     *          Property to convert
     * @return  Converted skin
     */
    @NotNull
    default TabList.Skin propertyToSkin(@NotNull Property property) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Creates new text component using given text.
     *
     * @param   text
     *          Component text
     * @return  Text component with given text
     */
    @NotNull
    default Component newTextComponent(@NotNull String text) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Adds sibling to a component.
     *
     * @param   parent
     *          Parent to add sibling to
     * @param   child
     *          Sibling to add
     */
    default void addSibling(@NotNull Component parent, @NotNull Component child) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Creates team register packet using given team.
     *
     * @param   team
     *          Team to register
     * @return  Team register packet using given team
     */
    @NotNull
    default Packet<?> registerTeam(@NotNull PlayerTeam team) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Creates team unregister packet using given team.
     *
     * @param   team
     *          Team to unregister
     * @return  Team unregister packet using given team
     */
    @NotNull
    default Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Creates team update packet using given team.
     *
     * @param   team
     *          Team to update
     * @return  Team update packet using given team
     */
    @NotNull
    default Packet<?> updateTeam(@NotNull PlayerTeam team) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Sends message to player.
     *
     * @param   player
     *          Player to send message to
     * @param   message
     *          Message to send
     */
    default void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Sends message to command source.
     *
     * @param   source
     *          Command source to send message to
     * @param   message
     *          Message to send
     */
    default void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Checks outgoing team packet in pipeline to potentially remove players from it.
     *
     * @param   packet
     *          Packet to check
     * @param   scoreboard
     *          Scoreboard of player who received the packet
     */
    default void checkTeamPacket(@NotNull Packet<?> packet, @NotNull FabricScoreboard scoreboard) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Returns {@code true} if packet is player info packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is player info packet, {@code false} if not
     */
    default boolean isPlayerInfo(@NotNull Packet<?> packet) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Processed player info packet for anti-override and similar.
     *
     * @param   receiver
     *          Player who received the packet
     * @param   packet
     *          Received packet
     */
    default void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder builder) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Returns player's world
     *
     * @param   player
     *          Player to get world of
     * @return  Player's world
     */
    @NotNull
    default Level getLevel(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Returns player's ping.
     *
     * @param   player
     *          Player to get ping of
     * @return  Player's ping
     */
    default int getPing(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Returns display slot of given display objective packet.
     *
     * @param   packet
     *          Display objective packet
     * @return  Display slot of packet
     */
    default int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Returns player's network channel.
     *
     * @param   player
     *          Player to get channel of
     * @return  Player's channel
     */
    @NotNull
    default Channel getChannel(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Returns server's current milliseconds per tick.
     *
     * @param   server
     *          Server to get MSPT value from
     * @return  Server's milliseconds per tick
     */
    default float getMSPT(@NotNull MinecraftServer server) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Objective newObjective(@NotNull String name, @NotNull Component displayName, @NotNull RenderType renderType, @Nullable TabComponent numberFormat) {
        throw new UnsupportedOperationException("Not implemented.");
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
    default Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable TabComponent numberFormat) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Sets style in a component to specified style.
     *
     * @param   component
     *          Component to change style of
     * @param   style
     *          Style to use
     */
    default void setStyle(@NotNull Component component, @NotNull Style style) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Logs console message as info.
     *
     * @param   message
     *          Message to log
     */
    default void logInfo(@NotNull TabComponent message) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Logs console message as warn.
     *
     * @param   message
     *          Message to log
     */
    default void logWarn(@NotNull TabComponent message) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Creates command source stack from ServerPlayer.
     *
     * @param   player
     *          Player to create command source stack from
     * @return  command source stack from player
     */
    @NotNull
    default CommandSourceStack createCommandSourceStack(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
