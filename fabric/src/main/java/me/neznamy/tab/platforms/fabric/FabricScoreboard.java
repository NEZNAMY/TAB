package me.neznamy.tab.platforms.fabric;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;

/**
 * Scoreboard implementation for Fabric using packets.
 */
public class FabricScoreboard extends SafeScoreboard<FabricTabPlayer> {

    private static final ChatFormatting[] formats = ChatFormatting.values();
    private static final net.minecraft.world.scores.Team.CollisionRule[] collisions = net.minecraft.world.scores.Team.CollisionRule.values();
    private static final net.minecraft.world.scores.Team.Visibility[] visibilities = net.minecraft.world.scores.Team.Visibility.values();
    private static final Scoreboard dummyScoreboard = new Scoreboard();

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public FabricScoreboard(FabricTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        net.minecraft.world.scores.Objective obj = FabricMultiVersion.newObjective(
                objective.getName(),
                objective.getTitle().convert(player.getVersion()),
                RenderType.values()[objective.getHealthDisplay().ordinal()],
                objective.getNumberFormat()
        );
        objective.setPlatformObjective(obj);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.REGISTER));
        player.sendPacket(FabricMultiVersion.setDisplaySlot(objective.getDisplaySlot().ordinal(), obj));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        player.sendPacket(new ClientboundSetObjectivePacket((net.minecraft.world.scores.Objective) objective.getPlatformObjective(), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        net.minecraft.world.scores.Objective obj = (net.minecraft.world.scores.Objective) objective.getPlatformObjective();
        obj.setDisplayName(objective.getTitle().convert(player.getVersion()));
        obj.setRenderType(RenderType.values()[objective.getHealthDisplay().ordinal()]);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        player.sendPacket(FabricMultiVersion.setScore(score.getObjective(), score.getHolder(), score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().convert(player.getVersion()),
                score.getNumberFormat()));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        player.sendPacket(FabricMultiVersion.removeScore(score.getObjective(), score.getHolder()));
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new PlayerTeam(dummyScoreboard, name);
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        updateTeamProperties(team);
        PlayerTeam t = (PlayerTeam) team.getPlatformTeam();
        t.getPlayers().addAll(team.getPlayers());
        player.sendPacket(FabricMultiVersion.registerTeam(t));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        player.sendPacket(FabricMultiVersion.unregisterTeam((PlayerTeam) team.getPlatformTeam()));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        updateTeamProperties(team);
        player.sendPacket(FabricMultiVersion.updateTeam((PlayerTeam) team.getPlatformTeam()));
    }

    private void updateTeamProperties(@NonNull Team team) {
        PlayerTeam t = (PlayerTeam) team.getPlatformTeam();
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
        t.setColor(formats[team.getColor().ordinal()]);
        t.setCollisionRule(collisions[team.getCollision().ordinal()]);
        t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        t.setPlayerPrefix(team.getPrefix().convert(player.getVersion()));
        t.setPlayerSuffix(team.getSuffix().convert(player.getVersion()));
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (isAntiOverrideScoreboard()) {
            if (packet instanceof ClientboundSetDisplayObjectivePacket display) {
                TAB.getInstance().getFeatureManager().onDisplayObjective(player, FabricMultiVersion.getDisplaySlot(display), display.objectiveName);
            }
            if (packet instanceof ClientboundSetObjectivePacket objective) {
                TAB.getInstance().getFeatureManager().onObjective(player, objective.method, objective.objectiveName);
            }
        }
        if (isAntiOverrideTeams()) {
            FabricMultiVersion.checkTeamPacket((Packet<?>) packet, this);
        }
    }
}
