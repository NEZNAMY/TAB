package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerInfoStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardDisplayObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardTeamStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.sorting.Sorting;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Pipeline injection for bukkit
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    @Override
    protected Channel getChannel(TabPlayer player) {
        BukkitTabPlayer bukkit = (BukkitTabPlayer) player;
        NMSStorage nms = NMSStorage.getInstance();
        try {
            if (nms.CHANNEL != null) return (Channel) nms.CHANNEL.get(nms.NETWORK_MANAGER.get(bukkit.getPlayerConnection()));
        } catch (final IllegalAccessException exception) {
            TAB.getInstance().getErrorManager().printError("Failed to get channel of " + bukkit.getName(), exception);
        }
        return null;
    }

    @Override
    public void onDisplayObjective(TabPlayer player, Object packet) throws IllegalAccessException {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                PacketPlayOutScoreboardDisplayObjectiveStorage.POSITION.getInt(packet),
                (String) PacketPlayOutScoreboardDisplayObjectiveStorage.OBJECTIVE_NAME.get(packet));
    }

    @Override
    public void onObjective(TabPlayer player, Object packet) throws IllegalAccessException {
        TAB.getInstance().getFeatureManager().onObjective(player,
                PacketPlayOutScoreboardObjectiveStorage.METHOD.getInt(packet),
                (String) PacketPlayOutScoreboardObjectiveStorage.OBJECTIVE_NAME.get(packet));
    }

    @Override
    public boolean isDisplayObjective(Object packet) {
        return PacketPlayOutScoreboardDisplayObjectiveStorage.CLASS.isInstance(packet);
    }

    @Override
    public boolean isObjective(Object packet) {
        return PacketPlayOutScoreboardObjectiveStorage.CLASS.isInstance(packet);
    }

    @Override
    public boolean isTeam(Object packet) {
        return PacketPlayOutScoreboardTeamStorage.CLASS.isInstance(packet);
    }

    @Override
    public boolean isPlayerInfo(Object packet) {
        return PacketPlayOutPlayerInfoStorage.CLASS.isInstance(packet) ||
                (PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket != null &&
                        PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket.isInstance(packet));
    }

    @Override
    public boolean isLogin(Object packet) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void modifyPlayers(Object packetPlayOutScoreboardTeam) throws ReflectiveOperationException {
        int action = PacketPlayOutScoreboardTeamStorage.ACTION.getInt(packetPlayOutScoreboardTeam);
        if (action == 1 || action == 2 || action == 4) return;
        Collection<String> players = (Collection<String>) PacketPlayOutScoreboardTeamStorage.PLAYERS.get(packetPlayOutScoreboardTeam);
        String teamName = (String) PacketPlayOutScoreboardTeamStorage.NAME.get(packetPlayOutScoreboardTeam);
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
        PacketPlayOutScoreboardTeamStorage.PLAYERS.set(packetPlayOutScoreboardTeam, newList);
    }
}