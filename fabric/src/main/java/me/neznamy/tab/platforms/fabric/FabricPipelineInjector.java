package me.neznamy.tab.platforms.fabric;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class FabricPipelineInjector extends NettyPipelineInjector {

    public FabricPipelineInjector() {
        super("packet_handler");
    }

    @Override
    @NotNull
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return FabricMultiVersion.getChannel.apply(((FabricTabPlayer)player).getPlayer());
    }

    @Override
    @SneakyThrows
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        int slot = FabricMultiVersion.getDisplaySlot.apply((ClientboundSetDisplayObjectivePacket) packet);
        String objective = (String) ReflectionUtils.getFields(packet.getClass(), String.class).get(0).get(packet);
        TAB.getInstance().getFeatureManager().onDisplayObjective(player, slot, objective);
    }

    @Override
    @SneakyThrows
    public void onObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        int action = ReflectionUtils.getFields(packet.getClass(), int.class).get(0).getInt(packet);
        String objective = (String) ReflectionUtils.getFields(packet.getClass(), String.class).get(0).get(packet);
        TAB.getInstance().getFeatureManager().onObjective(player, action, objective);
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
        return FabricMultiVersion.isTeamPacket.apply((Packet<?>) packet);
    }

    @Override
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void modifyPlayers(@NotNull Object teamPacket) {
        if (TAB.getInstance().getNameTagManager() == null) return;
        int action = ReflectionUtils.getInstanceFields(teamPacket.getClass(), int.class).get(0).getInt(teamPacket);
        if (action == 1 || action == 2 || action == 4) return;
        Field playersField = ReflectionUtils.getFields(teamPacket.getClass(), Collection.class).get(0);
        Collection<String> players = (Collection<String>) playersField.get(teamPacket);
        String teamName = String.valueOf(ReflectionUtils.getFields(teamPacket.getClass(), String.class).get(0).get(teamPacket));
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
        playersField.set(teamPacket, newList);
    }
}
