package me.neznamy.tab.platforms.bukkit.v1_14_R1;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
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

    private static final Field TeamPacket_NAME = ReflectionUtils.getFields(PacketPlayOutScoreboardTeam.class, String.class).get(0);
    private static final Field TeamPacket_ACTION = ReflectionUtils.getInstanceFields(PacketPlayOutScoreboardTeam.class, int.class).get(0);
    private static final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardTeam.class, Collection.class);

    private static final Field Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(PacketPlayOutScoreboardObjective.class, String.class).get(0);
    private static final Field Objective_METHOD = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardObjective.class, int.class);

    private static final Field DisplayObjective_OBJECTIVE_NAME = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardDisplayObjective.class, String.class);
    private static final Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardDisplayObjective.class, int.class);

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
                IScoreboardCriteria.DUMMY,
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
        obj.setDisplayName(objective.getTitle().convert());
        obj.setRenderType(IScoreboardCriteria.EnumScoreboardHealthDisplay.valueOf(objective.getHealthDisplay().name()));
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        sendPacket(new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.CHANGE,
                score.getObjective().getName(),
                score.getHolder(),
                score.getValue()
        ));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sendPacket(new PacketPlayOutScoreboardScore(
                ScoreboardServer.Action.REMOVE,
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
        t.getPlayerNameSet().addAll(team.getPlayers());
        sendPacket(new PacketPlayOutScoreboardTeam(t, TeamAction.CREATE));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sendPacket(new PacketPlayOutScoreboardTeam((ScoreboardTeam) team.getPlatformTeam(), TeamAction.REMOVE));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        updateTeamProperties(team);
        sendPacket(new PacketPlayOutScoreboardTeam((ScoreboardTeam) team.getPlatformTeam(), TeamAction.UPDATE));
    }

    private void updateTeamProperties(@NonNull Team team) {
        ScoreboardTeam t = (ScoreboardTeam) team.getPlatformTeam();
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setCanSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
        t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        t.setCollisionRule(collisions[team.getCollision().ordinal()]);
        t.setPrefix(team.getPrefix().convert());
        t.setSuffix(team.getSuffix().convert());
        t.setColor(EnumChatFormat.valueOf(team.getColor().name()));
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof PacketPlayOutScoreboardDisplayObjective) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(player, DisplayObjective_POSITION.getInt(packet),
                    (String) DisplayObjective_OBJECTIVE_NAME.get(packet));
        }
        if (packet instanceof PacketPlayOutScoreboardObjective) {
            TAB.getInstance().getFeatureManager().onObjective(player,
                    Objective_METHOD.getInt(packet), (String) Objective_OBJECTIVE_NAME.get(packet));
        }
        if (packet instanceof PacketPlayOutScoreboardTeam) {
            int action = TeamPacket_ACTION.getInt(packet);
            if (action != TeamAction.UPDATE) {
                Collection<String> players = (Collection<String>) TeamPacket_PLAYERS.get(packet);
                if (players == null) players = Collections.emptyList();
                TeamPacket_PLAYERS.set(packet, onTeamPacket(action, (String) TeamPacket_NAME.get(packet), players));
            }
        }
        return packet;
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
    }
}
