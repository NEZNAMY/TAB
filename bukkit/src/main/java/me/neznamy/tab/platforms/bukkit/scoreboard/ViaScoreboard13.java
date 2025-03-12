package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.util.ComponentUtil;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;

/**
 * Scoreboard implementation using ViaVersion packets
 * to unlock +1.13 player features on pre 1.13 servers.
 */
public class ViaScoreboard13 extends ViaScoreboard {

    /**
     * Constructs new instance with given player.
     *
     * @param player          Player this scoreboard will belong to
     */
    public ViaScoreboard13(@NonNull BukkitTabPlayer player) {
        this(player, Protocol1_12_2To1_13.class, ClientboundPackets1_13.SET_DISPLAY_OBJECTIVE, ClientboundPackets1_13.SET_OBJECTIVE, ClientboundPackets1_13.SET_SCORE, ClientboundPackets1_13.SET_PLAYER_TEAM);
    }

    protected ViaScoreboard13(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType setPlayerTeam) {
        super(player, protocol, setDisplayObjective, setObjective, setScore, setPlayerTeam);
    }

    @Override
    public void setScore(@NonNull Score score) {
        sendScoreUpdate(ScoreAction.CHANGE, score);
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sendScoreUpdate(ScoreAction.REMOVE, score);
    }

    protected void sendScoreUpdate(int action, @NonNull Score score) {
        final PacketWrapper packet = PacketWrapper.create(setScore, null, connection);

        // Entity name (player username in this case)
        packet.write(Types.STRING, score.getHolder());
        // Action
        packet.write(Types.VAR_INT, action);
        // Objective name (max 16 characters)
        packet.write(Types.STRING, score.getObjective().getName());
        if (action == 0) {
            // Value
            packet.write(Types.VAR_INT, score.getValue());
        }

        packet.scheduleSend(protocol);
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component) {
        packet.write(Types.COMPONENT, ComponentUtil.legacyToJson(component.toLegacyText()));
    }

    @Override
    protected void writeObjective(@NonNull PacketWrapper packet, @NonNull Objective objective) {
        packet.write(Types.BYTE, (byte) objective.getDisplaySlot().ordinal());
        packet.write(Types.STRING, objective.getName());
    }

    @Override
    protected void writeObjectiveDisplay(@NonNull PacketWrapper packet, @NonNull Objective objective) {
        // Objective value
        writeComponent(packet, objective.getTitle());
        // Type
        packet.write(Types.VAR_INT, objective.getHealthDisplay() == HealthDisplay.INTEGER ? 0 : 1);
    }
}
