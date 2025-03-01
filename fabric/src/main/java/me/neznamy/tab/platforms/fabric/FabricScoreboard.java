package me.neznamy.tab.platforms.fabric;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
        net.minecraft.world.scores.Objective obj = new net.minecraft.world.scores.Objective(
                dummyScoreboard,
                objective.getName(),
                ObjectiveCriteria.DUMMY,
                objective.getTitle().convert(),
                RenderType.values()[objective.getHealthDisplay().ordinal()],
                false,
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().toFixedFormat(FixedFormat::new)
        );
        objective.setPlatformObjective(obj);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.REGISTER));
        player.sendPacket(new ClientboundSetDisplayObjectivePacket(net.minecraft.world.scores.DisplaySlot.values()[objective.getDisplaySlot().ordinal()], obj));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        player.sendPacket(new ClientboundSetObjectivePacket((net.minecraft.world.scores.Objective) objective.getPlatformObjective(), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        net.minecraft.world.scores.Objective obj = (net.minecraft.world.scores.Objective) objective.getPlatformObjective();
        obj.setDisplayName(objective.getTitle().convert());
        obj.setRenderType(RenderType.values()[objective.getHealthDisplay().ordinal()]);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        player.sendPacket(new ClientboundSetScorePacket(
                score.getHolder(),
                score.getObjective().getName(),
                score.getValue(),
                Optional.ofNullable(score.getDisplayName() == null ? null : score.getDisplayName().convert()),
                Optional.ofNullable(score.getNumberFormat() == null ? null : score.getNumberFormat().toFixedFormat(FixedFormat::new)))
        );
    }

    @Override
    public void removeScore(@NonNull Score score) {
        player.sendPacket(new ClientboundResetScorePacket(score.getHolder(), score.getObjective().getName()));
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
        player.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(t, true));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        player.sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket((PlayerTeam) team.getPlatformTeam()));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        updateTeamProperties(team);
        player.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket((PlayerTeam) team.getPlatformTeam(), false));
    }

    private void updateTeamProperties(@NonNull Team team) {
        PlayerTeam t = (PlayerTeam) team.getPlatformTeam();
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
        t.setColor(formats[team.getColor().getLegacyColor().ordinal()]);
        t.setCollisionRule(collisions[team.getCollision().ordinal()]);
        t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        t.setPlayerPrefix(team.getPrefix().convert());
        t.setPlayerSuffix(team.getSuffix().convert());
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (isAntiOverrideScoreboard()) {
            if (packet instanceof ClientboundSetDisplayObjectivePacket display) {
                TAB.getInstance().getFeatureManager().onDisplayObjective(player, display.getSlot().ordinal(), display.objectiveName);
            }
            if (packet instanceof ClientboundSetObjectivePacket objective) {
                TAB.getInstance().getFeatureManager().onObjective(player, objective.method, objective.objectiveName);
            }
        }
        if (isAntiOverrideTeams()) {
            if (packet instanceof ClientboundSetPlayerTeamPacket team) {
                if (team.method == TeamAction.UPDATE) return;
                team.players = onTeamPacket(team.method, team.getName(), team.players);
            }
        }
    }
}
