package me.neznamy.tab.platforms.bukkit.v1_7_R4;

import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Scoreboard implementation using direct NMS code.
 */
public class NMSPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    private static final Scoreboard dummyScoreboard = new Scoreboard();

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
        ScoreboardObjective obj = new ScoreboardObjective(dummyScoreboard, objective.getName(), IScoreboardCriteria.b);
        obj.setDisplayName(maybeCut(objective.getTitle().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13));
        objective.setPlatformObjective(obj);
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.REGISTER));
    }

    @Override
    public void setDisplaySlot(@NonNull Objective objective) {
        sendPacket(new PacketPlayOutScoreboardDisplayObjective(objective.getDisplaySlot().ordinal(), (ScoreboardObjective) objective.getPlatformObjective()));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sendPacket(new PacketPlayOutScoreboardObjective((ScoreboardObjective) objective.getPlatformObjective(), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        ScoreboardObjective obj = (ScoreboardObjective) objective.getPlatformObjective();
        obj.setDisplayName(maybeCut(objective.getTitle().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13));
        sendPacket(new PacketPlayOutScoreboardObjective(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        ScoreboardScore s = new ScoreboardScore(dummyScoreboard, (ScoreboardObjective) score.getObjective().getPlatformObjective(), score.getHolder());
        s.setScore(score.getValue());
        sendPacket(new PacketPlayOutScoreboardScore(s, 0));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sendPacket(new PacketPlayOutScoreboardScore(score.getHolder()));
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new ScoreboardTeam(dummyScoreboard, name);
    }

    @Override
    @SuppressWarnings("unchecked")
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
        t.setPrefix(maybeCut(team.getPrefix().toLegacyText(), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13));
        t.setSuffix(maybeCut(team.getSuffix().toLegacyText(), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13));
    }

    @Override
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        // Pipeline injection is not available (netty is relocated)
        return packet;
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    private void sendPacket(@NotNull Packet packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
    }

    @NotNull
    private String maybeCut(@NonNull String string, int length) {
        if (player.getVersion().getMinorVersion() < 13 || TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
            return cutTo(string, length);
        }
        return string;
    }
}
