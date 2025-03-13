package me.neznamy.tab.platforms.bukkit.scoreboard.viaversion;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Scoreboard implementation using ViaVersion packets
 * to unlock +1.20.3 player features on pre 1.20.3 servers.
 */
public class ViaScoreboard1203 extends ViaScoreboard116 {

    private final PacketType resetScore;

    /**
     * Constructs new instance with given player.
     *
     * @param player          Player this scoreboard will belong to
     */
    public ViaScoreboard1203(@NonNull BukkitTabPlayer player) {
        this(player, Protocol1_20_2To1_20_3.class, ClientboundPackets1_20_3.SET_DISPLAY_OBJECTIVE, ClientboundPackets1_20_3.SET_OBJECTIVE, ClientboundPackets1_20_3.SET_SCORE, ClientboundPackets1_20_3.RESET_SCORE, ClientboundPackets1_20_3.SET_PLAYER_TEAM);
    }

    protected ViaScoreboard1203(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType resetScore, @NonNull PacketType setPlayerTeam) {
        super(player, protocol, setDisplayObjective, setObjective, setScore, setPlayerTeam);
        this.resetScore = resetScore;
    }

    @Override
    public void setScore(@NonNull Score score) {
        final PacketWrapper packet = PacketWrapper.create(setScore, null, connection);

        // Entity name (player username in this case)
        packet.write(Types.STRING, score.getHolder());
        // Objective name
        packet.write(Types.STRING, score.getObjective().getName());
        // Value
        packet.write(Types.VAR_INT, score.getValue());
        // Display name
        packet.write(Types.OPTIONAL_TAG, score.getDisplayName() == null ? null : score.getDisplayName().toViaVersionTag());
        // Number format
        writeNumberFormat(packet, score.getNumberFormat());

        packet.scheduleSend(protocol);
    }

    @Override
    public void removeScore(@NonNull Score score) {
        final PacketWrapper packet = PacketWrapper.create(resetScore, null, connection);

        // Entity name (player username in this case)
        packet.write(Types.STRING, score.getHolder());
        // Objective name
        packet.write(Types.OPTIONAL_STRING, score.getObjective().getName().isEmpty() ? null : score.getObjective().getName());

        packet.scheduleSend(protocol);
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component) {
        packet.write(Types.TAG, component.toViaVersionTag());
    }

    @Override
    protected void writeObjective(@NonNull PacketWrapper packet, @NonNull Objective objective) {
        packet.write(Types.VAR_INT, objective.getDisplaySlot().ordinal());
        packet.write(Types.STRING, objective.getName());
    }

    @Override
    protected void writeObjectiveDisplay(@NonNull PacketWrapper packet, @NonNull Objective objective) {
        super.writeObjectiveDisplay(packet, objective);
        writeNumberFormat(packet, objective.getNumberFormat());
    }

    protected void writeNumberFormat(@NonNull PacketWrapper packet, @Nullable TabComponent content) {
        // For now, only fixed format is supported by TAB
        if (content == null) {
            // Has number format
            packet.write(Types.BOOLEAN, false);
        } else {
            // Has number format
            packet.write(Types.BOOLEAN, true);
            // Fixed format
            packet.write(Types.VAR_INT, 2);
            // Content
            packet.write(Types.TAG, content.toViaVersionTag());
        }
    }
}