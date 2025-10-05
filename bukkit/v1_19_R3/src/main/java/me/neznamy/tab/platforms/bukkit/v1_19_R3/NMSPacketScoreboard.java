package me.neznamy.tab.platforms.bukkit.v1_19_R3;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

/**
 * Scoreboard implementation using direct NMS code.
 */
public class NMSPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    private static final ScoreboardTeamBase.EnumNameTagVisibility[] visibilities = ScoreboardTeamBase.EnumNameTagVisibility.values();
    private static final ScoreboardTeamBase.EnumTeamPush[] collisions = ScoreboardTeamBase.EnumTeamPush.values();
    private static final Scoreboard dummyScoreboard = new Scoreboard();

    private static final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardTeam.class, Collection.class);

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public NMSPacketScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        ScoreboardObjective obj = new ScoreboardObjective(
                dummyScoreboard,
                objective.getName(),
                IScoreboardCriteria.a,
                objective.getTitle().convert(),
                IScoreboardCriteria.EnumScoreboardHealthDisplay.values()[objective.getHealthDisplay().ordinal()]
        );
        objective.setPlatformObjective(obj);
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.REGISTER));
    }

    @Override
    public void setDisplaySlot(@NonNull Objective objective) {
        sendPacket(new PacketPlayOutScoreboardDisplayObjective(
                objective.getDisplaySlot().ordinal(),
                (ScoreboardObjective) objective.getPlatformObjective()
        ));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sendPacket(new PacketPlayOutScoreboardObjective((ScoreboardObjective) objective.getPlatformObjective(), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        ScoreboardObjective obj = (ScoreboardObjective) objective.getPlatformObjective();
        obj.a((IChatBaseComponent) objective.getTitle().convert());
        obj.a(IScoreboardCriteria.EnumScoreboardHealthDisplay.valueOf(objective.getHealthDisplay().name()));
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        sendPacket(new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.a,
                score.getObjective().getName(),
                score.getHolder(),
                score.getValue()
        ));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sendPacket(new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.b,
                score.getObjective().getName(),
                score.getHolder(),
                score.getValue()
        ));
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new ScoreboardTeam(dummyScoreboard, name);
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        updateTeamProperties(team);
        ScoreboardTeam t = (ScoreboardTeam) team.getPlatformTeam();
        t.g().addAll(team.getPlayers());
        sendPacket(PacketPlayOutScoreboardTeam.a(t, true));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sendPacket(PacketPlayOutScoreboardTeam.a((ScoreboardTeam) team.getPlatformTeam()));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        updateTeamProperties(team);
        sendPacket(PacketPlayOutScoreboardTeam.a((ScoreboardTeam) team.getPlatformTeam(), false));
    }

    private void updateTeamProperties(@NonNull Team team) {
        ScoreboardTeam t = (ScoreboardTeam) team.getPlatformTeam();
        t.a((team.getOptions() & 0x01) != 0);
        t.b((team.getOptions() & 0x02) != 0);
        t.a(visibilities[team.getVisibility().ordinal()]);
        t.a(collisions[team.getCollision().ordinal()]);
        t.b((IChatBaseComponent) team.getPrefix().convert());
        t.c(team.getSuffix().convert());
        t.a(EnumChatFormat.valueOf(team.getColor().name()));
    }

    @Override
    @SneakyThrows
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof PacketPlayOutScoreboardDisplayObjective) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(
                    player,
                    ((PacketPlayOutScoreboardDisplayObjective)packet).a(),
                    ((PacketPlayOutScoreboardDisplayObjective)packet).c()
            );
        }
        if (packet instanceof PacketPlayOutScoreboardObjective) {
            TAB.getInstance().getFeatureManager().onObjective(
                    player,
                    ((PacketPlayOutScoreboardObjective)packet).d(),
                    ((PacketPlayOutScoreboardObjective)packet).a()
            );
        }
        if (packet instanceof PacketPlayOutScoreboardTeam) {
            int action = getMethod((PacketPlayOutScoreboardTeam) packet);
            if (action != TeamAction.UPDATE) {
                Collection<String> players = ((PacketPlayOutScoreboardTeam) packet).e();
                if (players == null) players = Collections.emptyList();
                TeamPacket_PLAYERS.set(packet, onTeamPacket(action, ((PacketPlayOutScoreboardTeam)packet).d(), players));
            }
        }
        return packet;
    }

    private int getMethod(@NonNull PacketPlayOutScoreboardTeam team) {
        if (team.c() == PacketPlayOutScoreboardTeam.a.a) {
            return 0;
        } else if (team.c() == PacketPlayOutScoreboardTeam.a.b) {
            return 1;
        } else if (team.a() == PacketPlayOutScoreboardTeam.a.a) {
            return 3;
        } else if (team.a() == PacketPlayOutScoreboardTeam.a.b) {
            return 4;
        } else {
            return 2;
        }
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().b.a(packet);
    }
}
