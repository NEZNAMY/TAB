package me.neznamy.tab.platforms.fabric.v1_17_1;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class FabricPipelineInjector extends NettyPipelineInjector {

    public FabricPipelineInjector() {
        super("packet_handler");
    }

    @Override
    @Nullable
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        Connection c = (Connection) ReflectionUtils.getFields(ServerGamePacketListenerImpl.class, Connection.class)
                .get(0).get(((FabricTabPlayer)player).getPlayer().connection);
        return (Channel) ReflectionUtils.getFields(Connection.class, Channel.class).get(0).get(c);
    }

    @Override
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                ((ClientboundSetDisplayObjectivePacket) packet).getSlot(),
                String.valueOf(((ClientboundSetDisplayObjectivePacket)packet).getObjectiveName()));
    }

    @Override
    public void onObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onObjective(player,
                ((ClientboundSetObjectivePacket)packet).getMethod(),
                ((ClientboundSetObjectivePacket)packet).getObjectiveName());
    }

    @Override
    public boolean isDisplayObjective(@NotNull Object packet) {
        return packet instanceof ClientboundSetDisplayObjectivePacket;
    }

    @Override
    public boolean isObjective(@NotNull Object packet) {
        return packet instanceof ClientboundSetObjectivePacket;
    }

    @Override
    public boolean isTeam(@NotNull Object packet) {
        return packet instanceof ClientboundSetPlayerTeamPacket;
    }

    @Override
    public boolean isPlayerInfo(@NotNull Object packet) {
        return packet instanceof ClientboundPlayerInfoPacket;
    }

    @Override
    @SneakyThrows
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet0) {
        ClientboundPlayerInfoPacket packet = (ClientboundPlayerInfoPacket) packet0;
        for (ClientboundPlayerInfoPacket.PlayerUpdate nmsData : packet.getEntries()) {
            GameProfile profile = nmsData.getProfile();
            Field displayNameField = ReflectionUtils.getFields(ClientboundPlayerInfoPacket.PlayerUpdate.class, Component.class).get(0);
            Field latencyField = ReflectionUtils.getFields(ClientboundPlayerInfoPacket.PlayerUpdate.class, int.class).get(0);
            if (packet.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME) {
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, profile.getId());
                if (newDisplayName != null) displayNameField.set(nmsData, ((FabricTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion()));
            }
            if (packet.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY) {
                latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
            }
            if (packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
            }
        }
    }

    @Override
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }

    @Override
    @SneakyThrows
    public void modifyPlayers(@NotNull Object teamPacket) {
        if (TAB.getInstance().getNameTagManager() == null) return;
        ClientboundSetPlayerTeamPacket packet = (ClientboundSetPlayerTeamPacket) teamPacket;
        int action = ReflectionUtils.getInstanceFields(ClientboundSetPlayerTeamPacket.class, int.class).get(0).getInt(packet);
        if (action == 1 || action == 2 || action == 4) return;
        Collection<String> players = packet.getPlayers();
        String teamName = packet.getName();
        if (players == null) return;
        //creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
        Collection<String> newList = new ArrayList<>();
        for (String entry : players) {
            TabPlayer p = getPlayer(entry);
            if (p == null) {
                newList.add(entry);
                continue;
            }
            Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            String expectedTeam = sorting.getShortTeamName(p);
            if (expectedTeam == null) {
                newList.add(entry);
                continue;
            }
            if (!((NameTag)TAB.getInstance().getNameTagManager()).getDisableChecker().isDisabledPlayer(p) &&
                    !TAB.getInstance().getNameTagManager().hasTeamHandlingPaused(p) && !teamName.equals(expectedTeam)) {
                logTeamOverride(teamName, p.getName(), expectedTeam);
            } else {
                newList.add(entry);
            }
        }
        ReflectionUtils.getFields(ClientboundSetPlayerTeamPacket.class, Collection.class).get(0).set(packet, newList);
    }
}
