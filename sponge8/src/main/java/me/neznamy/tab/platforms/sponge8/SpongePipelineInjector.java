package me.neznamy.tab.platforms.sponge8;

import io.netty.channel.Channel;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.sponge8.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.sorting.Sorting;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Pipeline injection for sponge
 */
public class SpongePipelineInjector extends NettyPipelineInjector {

    private static Field channelField;

    static {
        try {
            (channelField = Connection.class.getDeclaredField("channel")).setAccessible(true);
        } catch (final ReflectiveOperationException exception) {
            TAB.getInstance().getErrorManager().criticalError("Failed to initialize sponge internal fields", exception);
        }
    }

    /** NMS data storage */
    private final NMSStorage nms = NMSStorage.getInstance();

    /**
     * Constructs new instance
     */
    public SpongePipelineInjector() {
        super("packet_handler");
    }

    @Override
    protected Channel getChannel(TabPlayer player) {
        final SpongeTabPlayer sponge = (SpongeTabPlayer) player;
        try {
            return (Channel) channelField.get(((ServerPlayer) sponge.getPlayer()).connection.connection);
        } catch (final ReflectiveOperationException exception) {
            TAB.getInstance().getErrorManager().criticalError("Failed to get channel for " + player.getName(), exception);
        }
        return null;
    }

    @Override
    public void onDisplayObjective(TabPlayer player, Object packet) throws IllegalAccessException {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                nms.ClientboundSetDisplayObjectivePacket_position.getInt(packet),
                (String) nms.ClientboundSetDisplayObjectivePacket_objectivename.get(packet));
    }

    @Override
    public void onObjective(TabPlayer player, Object packet) throws IllegalAccessException {
        TAB.getInstance().getFeatureManager().onObjective(player,
                nms.ClientboundSetObjectivePacket_action.getInt(packet),
                (String) nms.ClientboundSetObjectivePacket_objectivename.get(packet));
    }

    @Override
    public boolean isDisplayObjective(Object packet) {
        return packet instanceof ClientboundSetDisplayObjectivePacket;
    }

    @Override
    public boolean isObjective(Object packet) {
        return packet instanceof ClientboundSetObjectivePacket;
    }

    @Override
    public boolean isTeam(Object packet) {
        return packet instanceof ClientboundSetPlayerTeamPacket;
    }

    @Override
    public boolean isPlayerInfo(Object packet) {
        return packet instanceof ClientboundPlayerInfoPacket;
    }

    @Override
    public boolean isLogin(Object packet) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void modifyPlayers(Object packetPlayOutScoreboardTeam) throws ReflectiveOperationException {
        int action = nms.ClientboundSetPlayerTeamPacket_ACTION.getInt(packetPlayOutScoreboardTeam);
        if (action == 1 || action == 2 || action == 4) return;
        Collection<String> players = (Collection<String>) nms.ClientboundSetPlayerTeamPacket_PLAYERS.get(packetPlayOutScoreboardTeam);
        String teamName = (String) nms.ClientboundSetPlayerTeamPacket_NAME.get(packetPlayOutScoreboardTeam);
        if (players == null) return;
        //creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
        Collection<String> newList = new ArrayList<>();
        for (String entry : players) {
            TabPlayer p = getPlayer(entry);
            if (p == null) {
                newList.add(entry);
                continue;
            }
            Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            if (!((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) &&
                    !TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !teamName.equals(sorting.getShortTeamName(p))) {
                logTeamOverride(teamName, p.getName(), sorting.getShortTeamName(p));
            } else {
                newList.add(entry);
            }
        }
        nms.ClientboundSetPlayerTeamPacket_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
    }
}