package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.GameType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class FabricMultiVersion {

    private static final Class<?> infoPacketClass = ClientboundPlayerInfoUpdatePacket.class; // 1.19.3+
    //private static final Class<?> infoPacketClass = ClientboundPlayerInfoPacket.class; // 1.19.2-

    private static Field PACKET_ENTRIES;

    static {
        for (Field field : infoPacketClass.getDeclaredFields()) {
            if (field.getType() == List.class) {
                field.setAccessible(true);
                PACKET_ENTRIES = field;
            }
        }
    }

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
        return setEntries(new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList()), entries);
    }

    // 1.19 - 1.19.2
    /*public static Packet<?> build(FabricTabList.Action action, List<FabricTabList.Builder> entries) {
        // 1.19+
        List<?> list = entries.stream().map(entry ->
                new ClientboundPlayerInfoPacket.PlayerUpdate(
                        entry.createProfile(),
                        entry.getLatency(),
                        GameType.byId(entry.getGameMode()),
                        entry.getDisplayName()
                        //, null // 1.19 - 1.19.2
                )
        ).toList();
        return setEntries(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList()), list);
    }*/

    private static Packet<?> setEntries(Packet<?> packet, List<?> entries) {
        try {
            PACKET_ENTRIES.set(packet, entries);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        return packet;
    }

    public static void registerCommand() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> FabricTAB.onRegisterCommands(dispatcher)); // 1.19+
        //net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, $) -> FabricTAB.onRegisterCommands(dispatcher)); // 1.18.2-
    }
}
