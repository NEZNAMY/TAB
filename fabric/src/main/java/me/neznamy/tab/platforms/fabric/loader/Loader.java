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
    String getLevelName(@NotNull Level level);

    /**
     * Converts minecraft property class into TAB skin class.
     *
     * @param   property
     *          Property to convert
     * @return  Converted skin
     */
    @NotNull
    TabList.Skin propertyToSkin(@NotNull Property property);

    /**
     * Creates new text component using given text.
     *
     * @param   text
     *          Component text
     * @return  Text component with given text
     */
    @NotNull
    Component newTextComponent(@NotNull String text);

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
    Style convertModifier(@NotNull ChatModifier modifier, boolean modern);

    /**
     * Adds sibling to a component.
     *
     * @param   parent
     *          Parent to add sibling to
     * @param   child
     *          Sibling to add
     */
    void addSibling(@NotNull Component parent, @NotNull Component child);

    /**
     * Creates team register packet using given team.
     *
     * @param   team
     *          Team to register
     * @return  Team register packet using given team
     */
    @NotNull
    Packet<?> registerTeam(@NotNull PlayerTeam team);

    /**
     * Creates team unregister packet using given team.
     *
     * @param   team
     *          Team to unregister
     * @return  Team unregister packet using given team
     */
    @NotNull
    Packet<?> unregisterTeam(@NotNull PlayerTeam team);

    /**
     * Creates team update packet using given team.
     *
     * @param   team
     *          Team to update
     * @return  Team update packet using given team
     */
    @NotNull
    Packet<?> updateTeam(@NotNull PlayerTeam team);

    /**
     * Sends message to player.
     *
     * @param   player
     *          Player to send message to
     * @param   message
     *          Message to send
     */
    void sendMessage(@NotNull ServerPlayer player, @NotNull Component message);

    /**
     * Sends message to command source.
     *
     * @param   source
     *          Command source to send message to
     * @param   message
     *          Message to send
     */
    void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message);

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
    Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer);

    /**
     * Checks outgoing team packet in pipeline to potentially remove players from it.
     *
     * @param   packet
     *          Packet to check
     * @param   scoreboard
     *          Scoreboard of player who received the packet
     */
    void checkTeamPacket(@NotNull Packet<?> packet, @NotNull FabricScoreboard scoreboard);

    /**
     * Returns {@code true} if packet is player info packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if packet is player info packet, {@code false} if not
     */
    boolean isPlayerInfo(@NotNull Packet<?> packet);

    /**
     * Processed player info packet for anti-override and similar.
     *
     * @param   receiver
     *          Player who received the packet
     * @param   packet
     *          Received packet
     */
    void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet);

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
    Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder builder);

    /**
     * Returns player's world
     *
     * @param   player
     *          Player to get world of
     * @return  Player's world
     */
    @NotNull
    Level getLevel(@NotNull ServerPlayer player);

    /**
     * Returns player's ping.
     *
     * @param   player
     *          Player to get ping of
     * @return  Player's ping
     */
    int getPing(@NotNull ServerPlayer player);

    /**
     * Returns display slot of given display objective packet.
     *
     * @param   packet
     *          Display objective packet
     * @return  Display slot of packet
     */
    int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet);

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
    Packet<?> setDisplaySlot(int slot, @NotNull Objective objective);

    /**
     * Returns player's network channel.
     *
     * @param   player
     *          Player to get channel of
     * @return  Player's channel
     */
    @NotNull
    Channel getChannel(@NotNull ServerPlayer player);

    /**
     * Returns server's current milliseconds per tick.
     *
     * @param   server
     *          Server to get MSPT value from
     * @return  Server's milliseconds per tick
     */
    float getMSPT(@NotNull MinecraftServer server);

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
    Packet<?> removeScore(@NotNull String objective, @NotNull String holder);

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
    Objective newObjective(@NotNull String name, @NotNull Component displayName, @NotNull RenderType renderType, @Nullable TabComponent numberFormat);

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
    Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable TabComponent numberFormat);

    /**
     * Sets style in a component to specified style.
     *
     * @param   component
     *          Component to change style of
     * @param   style
     *          Style to use
     */
    void setStyle(@NotNull Component component, @NotNull Style style);

    /**
     * Logs console message as info.
     *
     * @param   message
     *          Message to log
     */
    void logInfo(@NotNull TabComponent message);

    /**
     * Logs console message as warn.
     *
     * @param   message
     *          Message to log
     */
    void logWarn(@NotNull TabComponent message);
}
