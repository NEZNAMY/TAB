package me.neznamy.tab.platforms.bukkit.scoreboard.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import lombok.NonNull;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;

import java.util.concurrent.TimeUnit;

/**
 * Scoreboard abstract implementation using ViaVersion packets,
 * focused to unlock new features on older versions, and also
 * optimize packet conversion between versions.
 */
public abstract class ViaScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    private static final long DELAY = 500;
    private static final Object DUMMY = new Object();

    protected final Class<? extends Protocol> protocol;
    private final PacketType setDisplayObjective;
    private final PacketType setObjective;
    protected final PacketType setScore;
    private final PacketType setPlayerTeam;
    /** User connection this tablist belongs to */
    protected final UserConnection connection;

    private transient boolean delayed = true;

    /**
     * Constructs new instance with given player.
     *
     * @param player
     *        Player this scoreboard will belong to
     * @param protocol
     *        Protocol to be sent packets through
     * @param setDisplayObjective
     *        Objective creation packet
     * @param setObjective
     *        Objective update packet
     * @param setScore
     *        Score update packet
     * @param setPlayerTeam
     *        Player team packet
     */
    public ViaScoreboard(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType setPlayerTeam) {
        super(player);
        this.protocol = protocol;
        this.setDisplayObjective = setDisplayObjective;
        this.setObjective = setObjective;
        this.setScore = setScore;
        this.setPlayerTeam = setPlayerTeam;
        this.connection = Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId());
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        sendObjectiveUpdate(ObjectiveAction.REGISTER, objective);

        final PacketWrapper packet = PacketWrapper.create(setDisplayObjective, null, connection);

        writeObjective(packet, objective);

        send(packet);
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sendObjectiveUpdate(ObjectiveAction.UNREGISTER, objective);
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        sendObjectiveUpdate(ObjectiveAction.UPDATE, objective);
    }

    @Override
    public @NonNull Object createTeam(@NonNull String name) {
        // This implementation does not use team objects
        return DUMMY;
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        sendTeamUpdate(TeamAction.CREATE, team);
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sendTeamUpdate(TeamAction.REMOVE, team);
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        sendTeamUpdate(TeamAction.UPDATE, team);
    }

    protected void sendObjectiveUpdate(int mode, @NonNull Objective objective) {
        final PacketWrapper packet = PacketWrapper.create(setObjective, null, connection);

        // Objective name
        packet.write(Types.STRING, objective.getName());
        // Mode
        packet.write(Types.BYTE, (byte) mode);
        // Optional
        if (mode == ObjectiveAction.REGISTER || mode == ObjectiveAction.UPDATE) {
            writeObjectiveDisplay(packet, objective);
        }

        send(packet);
    }

    @SuppressWarnings("fallthrough")
    protected void sendTeamUpdate(int mode, @NonNull Team team) {
        final PacketWrapper packet = PacketWrapper.create(setPlayerTeam, null, connection);

        // Team name (max 16 characters on pre 1.20.3)
        packet.write(Types.STRING, team.getName());
        // Method
        packet.write(Types.BYTE, (byte) mode);
        switch (mode) {
            case TeamAction.REMOVE:
                break;
            case TeamAction.CREATE:
            case TeamAction.UPDATE:
                // Team display name
                writeComponent(packet, SimpleTextComponent.text(team.getName()));
                // Friendly flags
                packet.write(Types.BYTE, (byte) team.getOptions());
                // Name tag visibility
                packet.write(Types.STRING, team.getVisibility().toString());
                // Collision rule
                packet.write(Types.STRING, team.getCollision().toString());
                // Team color
                packet.write(Types.VAR_INT, team.getColor().getLegacyColor().ordinal());
                // Team prefix
                writeComponent(packet, team.getPrefix());
                // Team suffix
                writeComponent(packet, team.getSuffix());
                if (mode == TeamAction.UPDATE) {
                    break;
                }
                // Intentional fallthrough for CREATE mode
            case TeamAction.ADD_PLAYER:
            case TeamAction.REMOVE_PLAYER:
                // Entities
                packet.write(Types.VAR_INT, team.getPlayers().size());
                for (String entity : team.getPlayers()) {
                    // Entity (player username in this case) (max 40 characters on pre 1.20.3)
                    packet.write(Types.STRING, entity);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid team update mode: " + mode);
        }

        send(packet);
    }

    protected abstract void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component);

    protected abstract void writeObjective(@NonNull PacketWrapper packet, @NonNull Objective objective);

    protected abstract void writeObjectiveDisplay(@NonNull PacketWrapper packet, @NonNull Objective objective);

    protected void send(@NonNull PacketWrapper packet) {
        if (delayed) {
            connection.getChannel().eventLoop().schedule(() -> {
                delayed = false;
                packet.send(protocol);
            }, DELAY, TimeUnit.MILLISECONDS);
        } else {
            packet.scheduleSend(protocol);
        }
    }
}
