package me.neznamy.tab.platforms.fabric;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;

import java.util.HashMap;
import java.util.Map;

/**
 * Scoreboard implementation for Fabric using packets.
 */
public class FabricScoreboard extends SafeScoreboard<FabricTabPlayer> {

    private static final net.minecraft.world.scores.Scoreboard dummyScoreboard = new net.minecraft.world.scores.Scoreboard();

    private final Map<String, net.minecraft.world.scores.Objective> objectives = new HashMap<>();

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
                toComponent(objective.getTitle()),
                RenderType.values()[objective.getHealthDisplay().ordinal()],
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().convert(player.getVersion())
        );
        objectives.put(objective.getName(), obj);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.REGISTER));
        player.sendPacket(FabricMultiVersion.setDisplaySlot(objective.getDisplaySlot().ordinal(), objectives.get(objective.getName())));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        player.sendPacket(new ClientboundSetObjectivePacket(objectives.remove(objective.getName()), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        net.minecraft.world.scores.Objective obj = objectives.get(objective.getName());
        obj.setDisplayName(toComponent(objective.getTitle()));
        obj.setRenderType(RenderType.values()[objective.getHealthDisplay().ordinal()]);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        player.sendPacket(FabricMultiVersion.setScore(score.getObjective(), score.getHolder(), score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().convert(player.getVersion()),
                score.getNumberFormat() == null ? null : score.getNumberFormat().convert(player.getVersion())));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        player.sendPacket(FabricMultiVersion.removeScore(score.getObjective(), score.getHolder()));
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        PlayerTeam t = new PlayerTeam(dummyScoreboard, team.getName());
        t.setAllowFriendlyFire((team.getOptions() & 0x01) > 0);
        t.setSeeFriendlyInvisibles((team.getOptions() & 0x02) > 0);
        t.setColor(ChatFormatting.valueOf(team.getColor().name()));
        t.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.valueOf(team.getCollision().name()));
        t.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.valueOf(team.getVisibility().name()));
        t.setPlayerPrefix(toComponent(team.getPrefix()));
        t.setPlayerSuffix(toComponent(team.getSuffix()));
        t.getPlayers().addAll(team.getPlayers());
        player.sendPacket(FabricMultiVersion.registerTeam(t));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        player.sendPacket(FabricMultiVersion.unregisterTeam(new PlayerTeam(dummyScoreboard, team.getName())));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        PlayerTeam t = new PlayerTeam(dummyScoreboard, team.getName());
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
        t.setColor(ChatFormatting.valueOf(team.getColor().name()));
        t.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.valueOf(team.getCollision().name()));
        t.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.valueOf(team.getVisibility().name()));
        t.setPlayerPrefix(toComponent(team.getPrefix()));
        t.setPlayerSuffix(toComponent(team.getSuffix()));
        player.sendPacket(FabricMultiVersion.updateTeam(t));
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

    @NonNull
    private Component toComponent(@NonNull String string) {
        return TabComponent.optimized(string).convert(player.getVersion());
    }
}
