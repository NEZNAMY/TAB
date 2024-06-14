package me.neznamy.tab.shared.platform.impl;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.SetDisplayObjective;
import me.neznamy.tab.shared.proxy.message.outgoing.SetObjective;
import me.neznamy.tab.shared.proxy.message.outgoing.SetScore;
import me.neznamy.tab.shared.proxy.message.outgoing.SetScoreboardTeam;

/**
 * Scoreboard handler using bridge to encode the packets.
 */
public class BridgeScoreboard extends SafeScoreboard<ProxyTabPlayer> {

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this scoreboard belongs to
     */
    public BridgeScoreboard(@NonNull ProxyTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        player.sendPluginMessage(new SetObjective(
                objective.getName(),
                ObjectiveAction.REGISTER,
                objective.getTitle().toFlatText(),
                objective.getHealthDisplay().ordinal(),
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().serialize(player.getVersion()))
        );
        player.sendPluginMessage(new SetDisplayObjective(objective.getDisplaySlot(), objective.getName()));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        player.sendPluginMessage(new SetObjective(objective.getName()));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        player.sendPluginMessage(new SetObjective(
                objective.getName(),
                ObjectiveAction.UPDATE,
                objective.getTitle().toFlatText(),
                objective.getHealthDisplay().ordinal(),
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().serialize(player.getVersion()))
        );
    }

    @Override
    public void setScore(@NonNull Score score) {
        player.sendPluginMessage(new SetScore(
                score.getObjective(),
                ScoreAction.CHANGE,
                score.getHolder(),
                score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().serialize(player.getVersion()),
                score.getNumberFormat() == null ? null : score.getNumberFormat().serialize(player.getVersion())
        ));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        player.sendPluginMessage(new SetScore(score.getObjective(), score.getHolder()));
    }

    @Override
    public Object createTeam(@NonNull String name) {
        return new Object(); // This implementation does not use team objects
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        player.sendPluginMessage(new SetScoreboardTeam(
                team.getName(),
                TeamAction.CREATE, 
                team.getPrefix().toFlatText(),
                team.getSuffix().toFlatText(),
                team.getOptions(),
                team.getVisibility().toString(),
                team.getCollision().toString(),
                team.getColor().ordinal(),
                team.getPlayers()
        ));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        player.sendPluginMessage(new SetScoreboardTeam(team.getName()));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        player.sendPluginMessage(new SetScoreboardTeam(team.getName(),
                TeamAction.UPDATE,
                team.getPrefix().toFlatText(),
                team.getSuffix().toFlatText(),
                team.getOptions(),
                team.getVisibility().toString(),
                team.getCollision().toString(),
                team.getColor().ordinal(),
                null
        ));
    }
}
