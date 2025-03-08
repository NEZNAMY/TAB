package me.neznamy.tab.platforms.viaversion.scoreboard;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * Scoreboard implementation using ViaVersion packets
 * to unlock +1.16 player features on pre 1.16 servers.
 *
 * @param <P> Platform's TabPlayer class
 */
public class ViaScoreboard16<P extends TabPlayer> extends ViaScoreboard13<P> {

    /**
     * Constructs new instance with given player.
     *
     * @param player          Player this scoreboard will belong to
     */
    public ViaScoreboard16(@NonNull P player) {
        this(player, Protocol1_15_2To1_16.class, ClientboundPackets1_16.SET_DISPLAY_OBJECTIVE, ClientboundPackets1_16.SET_OBJECTIVE, ClientboundPackets1_16.SET_SCORE, ClientboundPackets1_16.SET_PLAYER_TEAM);
    }

    protected ViaScoreboard16(@NonNull P player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType setPlayerTeam) {
        super(player, protocol, setDisplayObjective, setObjective, setScore, setPlayerTeam);
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component) {
        packet.write(Types.COMPONENT, component.toViaVersion());
    }
}
