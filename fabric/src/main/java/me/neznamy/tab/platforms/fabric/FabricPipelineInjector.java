package me.neznamy.tab.platforms.fabric;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import org.jetbrains.annotations.NotNull;

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
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }
}
