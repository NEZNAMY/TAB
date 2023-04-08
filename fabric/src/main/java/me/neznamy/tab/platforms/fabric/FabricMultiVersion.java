package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class allows user to change methods based on target server version.
 * All methods have several implementations, choose the one based on server version.
 */
public class FabricMultiVersion {

    private static final UUID SYSTEM_ID = new UUID(0, 0);
    public static final BiConsumer<FabricTabPlayer, IChatBaseComponent> sendMessage;
    public static final BiConsumer<CommandSourceStack, Component> sendMessage2;
    public static final Runnable registerCommand;
    public static final BiConsumer<ServerBossEvent, Float> setProgress;
    public static final Function<PlayerTeam, ClientboundSetPlayerTeamPacket> registerTeam;
    public static final Function<PlayerTeam, ClientboundSetPlayerTeamPacket> unregisterTeam;
    public static final Function<PlayerTeam, ClientboundSetPlayerTeamPacket> updateTeam;
    public static final BiFunction<Component, Component, Packet<?>> setHeaderAndFooter;

    static {
        // 1.19+

        sendMessage = (player, message) -> player.getPlayer().sendSystemMessage(FabricTAB.toComponent(message, player.getVersion()));
        sendMessage2 = CommandSourceStack::sendSystemMessage;
        registerCommand = () -> net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, $, $$) -> FabricTAB.onRegisterCommands(dispatcher));
        setProgress = ServerBossEvent::setProgress;
        registerTeam = team -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        unregisterTeam = ClientboundSetPlayerTeamPacket::createRemovePacket;
        updateTeam = team -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
        setHeaderAndFooter = ClientboundTabListPacket::new;

        // 1.17 - 1.18.2
/*
        sendMessage = (player, message) -> player.getPlayer().sendMessage(FabricTAB.toComponent(message, player.getVersion()), SYSTEM_ID);
        sendMessage2 = (source, message) -> source.sendSuccess(message, false);
        registerCommand = () -> net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register(
                (dispatcher, $) -> FabricTAB.onRegisterCommands(dispatcher));
        setProgress = ServerBossEvent::setProgress;
        registerTeam = team -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        unregisterTeam = ClientboundSetPlayerTeamPacket::createRemovePacket;
        updateTeam = team -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
        setHeaderAndFooter = ClientboundTabListPacket::new;
*/
        // 1.14 - 1.16.5
/*
        sendMessage = (player, message) -> player.getPlayer().sendMessage(FabricTAB.toComponent(message, player.getVersion())
                //, SYSTEM_ID // 1.16 - 1.16.5
        );
        sendMessage2 = (source, message) -> source.sendSuccess(message, false);
        registerCommand = () -> net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register(
                (dispatcher, $) -> FabricTAB.onRegisterCommands(dispatcher));
        setProgress = ServerBossEvent::setPercent;
        registerTeam = team -> new ClientboundSetPlayerTeamPacket(team, 0);
        unregisterTeam = team -> new ClientboundSetPlayerTeamPacket(team, 1);
        updateTeam = team -> new ClientboundSetPlayerTeamPacket(team, 2);
        setHeaderAndFooter = (header, footer) -> {
            ClientboundTabListPacket packet = new ClientboundTabListPacket();
            packet.header = header;
            packet.footer = footer;
            return packet;
        };
*/
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
    /*public static Packet<?> build(FabricTabList.Action action, List<FabricTabList.Builder> entries) {
        List<ClientboundPlayerInfoPacket.PlayerUpdate> list = entries.stream().map(entry ->
                new ClientboundPlayerInfoPacket
                        //() // 1.16.5-
                        .
                        //new // 1.16.5-
                        PlayerUpdate(
                        entry.createProfile(),
                        entry.getLatency(),
                        GameType.byId(entry.getGameMode()),
                        entry.getDisplayName()
                        //, null // 1.19 - 1.19.2
                )
        ).toList();
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
        packet.entries = list;
        return packet;
    }*/
}
