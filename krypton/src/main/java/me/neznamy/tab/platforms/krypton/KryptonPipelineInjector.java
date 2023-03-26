package me.neznamy.tab.platforms.krypton;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.WrappedChatComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.kryptonmc.api.auth.GameProfile;
import org.kryptonmc.api.world.GameMode;
import org.kryptonmc.krypton.network.NioConnection;
import org.kryptonmc.krypton.network.handlers.PacketHandler;
import org.kryptonmc.krypton.network.interceptor.PacketInterceptor;
import org.kryptonmc.krypton.network.interceptor.PacketInterceptorRegistry;
import org.kryptonmc.krypton.packet.GenericPacket;
import org.kryptonmc.krypton.packet.InboundPacket;
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate;
import org.kryptonmc.krypton.util.enumhelper.GameModes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KryptonPipelineInjector extends PipelineInjector {

    private final TabPacketInterceptor interceptor = new TabPacketInterceptor();
    private final Map<NioConnection, TabPlayer> players = new ConcurrentHashMap<>();

    @Override
    public void inject(TabPlayer player) {
        if (players.isEmpty()) {
            // If it's empty, no players have been registered yet, and we should add the interceptor
            PacketInterceptorRegistry.INSTANCE.register(interceptor);
        }
        players.put(((KryptonTabPlayer)player).getPlayer().getConnection(), player);
    }

    @Override
    public void uninject(TabPlayer player) {
        players.remove(((KryptonTabPlayer)player).getPlayer().getConnection());
        if (players.isEmpty()) {
            // If it's empty, we unregistered the last player, and we should remove the interceptor
            PacketInterceptorRegistry.INSTANCE.unregister(interceptor);
        }
    }

    public class TabPacketInterceptor implements PacketInterceptor {

        @Override
        public GenericPacket onSend(@NotNull NioConnection connection, @NotNull GenericPacket packet) {
            TabPlayer player = players.get(connection);
            if (player == null) return packet; // Packet needs to be returned unmodified to be sent
            if (packet instanceof PacketOutPlayerInfoUpdate) return rewritePlayerInfo(player, (PacketOutPlayerInfoUpdate) packet);
            TAB.getInstance().getFeatureManager().onPacketSend(player, packet);
            return packet;
        }

        private PacketOutPlayerInfoUpdate rewritePlayerInfo(TabPlayer receiver, PacketOutPlayerInfoUpdate packet) {
            EnumSet<PacketOutPlayerInfoUpdate.Action> actions = packet.actions();
            List<PacketOutPlayerInfoUpdate.Entry> newEntries = null;

            for (PacketOutPlayerInfoUpdate.Entry entry : packet.entries()) {
                GameProfile profile = entry.profile();
                GameMode gameMode = entry.gameMode();
                int latency = entry.latency();
                Component displayName = entry.displayName();
                boolean updated = false;

                if (actions.contains(PacketOutPlayerInfoUpdate.Action.UPDATE_GAME_MODE)) {
                    int newGameMode = TAB.getInstance().getFeatureManager().onGameModeChange(receiver, profile.uuid(), gameMode.ordinal());
                    if (newGameMode != gameMode.ordinal()) {
                        updated = true;
                        gameMode = GameModes.fromId(newGameMode);
                    }
                }
                if (actions.contains(PacketOutPlayerInfoUpdate.Action.UPDATE_LATENCY)) {
                    int newLatency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.uuid(), latency);
                    if (newLatency != latency) {
                        updated = true;
                        latency = newLatency;
                    }
                }
                if (actions.contains(PacketOutPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME)) {
                    IChatBaseComponent input = displayName == null ? null : new WrappedChatComponent(displayName);
                    IChatBaseComponent output = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, profile.uuid(), input);
                    if (output != input) {
                        updated = true;
                        displayName = convertComponent(output, receiver.getVersion());
                    }
                }
                if (actions.contains(PacketOutPlayerInfoUpdate.Action.ADD_PLAYER)) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.uuid(), profile.name());
                }
                if (!updated) {
                    // Don't bother rewriting the entry if nothing was changed
                    continue;
                }
                if (gameMode == null) gameMode = GameMode.SURVIVAL;
                PacketOutPlayerInfoUpdate.Entry result = new PacketOutPlayerInfoUpdate.Entry(profile.uuid(), profile, entry.listed(), latency, gameMode, displayName, entry.chatSession());
                if (newEntries == null) newEntries = new ArrayList<>();
                newEntries.add(result);
            }

            if (newEntries == null) {
                // Don't bother rewriting the packet if there were no updated entries
                return packet;
            }
            return new PacketOutPlayerInfoUpdate(packet.actions(), newEntries);
        }

        private Component convertComponent(IChatBaseComponent component, ProtocolVersion clientVersion) {
            if (component instanceof WrappedChatComponent) return (Component) ((WrappedChatComponent) component).getOriginalComponent();
            return GsonComponentSerializer.gson().deserialize(component.toString(clientVersion));
        }

        @Override
        public <H extends PacketHandler> InboundPacket<H> onReceive(@NotNull NioConnection connection, @NotNull InboundPacket<H> packet) {
            return packet;
        }
    }
}
