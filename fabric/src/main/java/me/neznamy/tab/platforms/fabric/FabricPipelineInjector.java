package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class FabricPipelineInjector extends NettyPipelineInjector {

    public FabricPipelineInjector() {
        super("packet_handler");
    }

    @Override
    @Nullable
    protected Channel getChannel(@NotNull TabPlayer player) {
        return ((FabricTabPlayer)player).getPlayer().connection.connection.channel;
    }

    @Override
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                FabricMultiVersion.getSlot((ClientboundSetDisplayObjectivePacket) packet),
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
        return FabricMultiVersion.isPlayerInfo(packet);
    }

    @Override
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet0) {
        // Comment this entire method out when compiling with 1.19.2-, adding compatibility would be tough and not worth,
        // as nobody needs these features on Fabric anyway

        ClientboundPlayerInfoUpdatePacket packet = (ClientboundPlayerInfoUpdatePacket) packet0;
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = packet.actions();
        List<ClientboundPlayerInfoUpdatePacket.Entry> updatedList = new ArrayList<>();
        for (ClientboundPlayerInfoUpdatePacket.Entry nmsData : packet.entries()) {
            GameProfile profile = nmsData.profile();
            Component displayName = nmsData.displayName();
            int latency = nmsData.latency();
            if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, nmsData.profileId());
                if (newDisplayName != null) displayName = ((FabricTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion());
            }
            if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)) {
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, nmsData.profileId(), latency);
            }
            if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, nmsData.profileId(), profile.getName());
            }
            updatedList.add(new ClientboundPlayerInfoUpdatePacket.Entry(nmsData.profileId(), profile, nmsData.listed(), latency, nmsData.gameMode(), displayName, nmsData.chatSession()));
        }
        packet.entries = updatedList;
    }

    @Override
    public void modifyPlayers(@NotNull Object teamPacket) {
        if (TAB.getInstance().getNameTagManager() == null) return;
        ClientboundSetPlayerTeamPacket packet = (ClientboundSetPlayerTeamPacket) teamPacket;
        int action = packet.method;
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
        packet.players = newList;
    }
}
