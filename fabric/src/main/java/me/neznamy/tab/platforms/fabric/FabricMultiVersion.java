package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.GameType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class FabricMultiVersion {

    public static void sendMessage(FabricTabPlayer player, IChatBaseComponent message) {
        player.getPlayer().sendSystemMessage(FabricTAB.toComponent(message, player.getVersion())); // 1.19+
        //player.getPlayer().sendMessage(FabricTAB.toComponent(message, player.getVersion()), new UUID(0,0)); // 1.18.2-
    }

    public static void sendConsoleMessage(String message) {
        Component component = Component.Serializer.fromJson(IChatBaseComponent.optimizedComponent(message).toString());
        FabricTAB.getServer().sendSystemMessage(component); // 1.19+
        //FabricTAB.getServer().sendMessage(component, new UUID(0,0)); // 1.18.2-
    }

    public static void sendMessage(CommandSourceStack source, String message) {
        Component component = Component.Serializer.fromJson(IChatBaseComponent.optimizedComponent(message).toString());
        source.sendSystemMessage(component); // 1.19+
        //source.sendSuccess(component, false); // 1.18.2-
    }

    // 1.19.3+
    public static Packet<?> build(FabricTabList.Action action, List<FabricTabList.Builder> entries) {
        if (action == FabricTabList.Action.REMOVE_PLAYER) {
            return new ClientboundPlayerInfoRemovePacket(entries.stream().map(FabricTabList.Builder::getId).toList());
        }
        List<ClientboundPlayerInfoUpdatePacket.Entry> list = entries.stream().map(entry ->
                new ClientboundPlayerInfoUpdatePacket.Entry(
                        entry.getId(),
                        entry.createProfile(),
                        entry.isListed(),
                        entry.getLatency(),
                        GameType.byId(entry.getGameMode()),
                        entry.getDisplayName(),
                        null
                )
        ).toList();

        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = action == FabricTabList.Action.ADD_PLAYER ?
                EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class) :
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.valueOf(action.name()));
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
        packet.entries = list;
        return packet;
    }

    // 1.19.2-
    /*
    public static Packet<?> build(FabricTabList.Action action, List<FabricTabList.Builder> entries) {
        // 1.19+
        List<ClientboundPlayerInfoPacket.PlayerUpdate> list = entries.stream().map(entry ->
                new ClientboundPlayerInfoPacket.PlayerUpdate(
                        entry.createProfile(),
                        entry.getLatency(),
                        GameType.byId(entry.getGameMode()),
                        entry.getDisplayName()
                        , null // 1.19 - 1.19.2
                )
        ).toList();
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
        packet.entries = list;
        return packet;
    }
     */

    public static void registerCommand() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> FabricTAB.onRegisterCommands(dispatcher)); // 1.19+
        //net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, $) -> FabricTAB.onRegisterCommands(dispatcher)); // 1.18.2-
    }
}
