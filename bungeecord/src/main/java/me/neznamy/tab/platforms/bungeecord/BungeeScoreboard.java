package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.NumberFormat;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.ScoreboardScoreReset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Scoreboard handler for BungeeCord. Because it does not offer
 * any Scoreboard API and the scoreboard class it has is just a
 * downstream tracker, we need to use packets.
 */
public class BungeeScoreboard extends SafeScoreboard<BungeeTabPlayer> {

    /** Version with a minor team recode */
    private final int TEAM_REWORK_VERSION = 13;

    /**
     * Constructs new instance with given parameter
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public BungeeScoreboard(@NonNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        sendObjectivePacket(objective, (byte) ObjectiveAction.REGISTER);
    }

    @Override
    public void setDisplaySlot(@NonNull Objective objective) {
        player.sendPacket(new ScoreboardDisplay(objective.getDisplaySlot().ordinal(), objective.getName()));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sendObjectivePacket(objective, (byte) ObjectiveAction.UNREGISTER);
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        sendObjectivePacket(objective, (byte) ObjectiveAction.UPDATE);
    }

    private void sendObjectivePacket(@NonNull Objective objective, byte action) {
        player.sendPacket(new ScoreboardObjective(
                objective.getName(),
                either(objective.getTitle(), Limitations.SCOREBOARD_TITLE_PRE_1_13),
                ScoreboardObjective.HealthDisplay.values()[objective.getHealthDisplay().ordinal()],
                action,
                numberFormat(objective.getNumberFormat())
        ));
    }

    @Override
    public void setScore(@NonNull Score score) {
        player.sendPacket(new ScoreboardScore(
                score.getHolder(),
                (byte) ScoreAction.CHANGE,
                score.getObjective().getName(),
                score.getValue(),
                score.getDisplayName() == null ? null : player.getPlatform().transformComponent(score.getDisplayName(), player.getVersion()),
                numberFormat(score.getNumberFormat())
        ));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
            player.sendPacket(new ScoreboardScoreReset(score.getHolder(), score.getObjective().getName()));
        } else {
            player.sendPacket(new ScoreboardScore(score.getHolder(), (byte) ScoreAction.REMOVE, score.getObjective().getName(), 0, null, null));
        }
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new Object(); // This implementation does not use team objects
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        sendTeamPacket(team, (byte) TeamAction.CREATE);
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sendTeamPacket(team, (byte) TeamAction.REMOVE);
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        sendTeamPacket(team, (byte) TeamAction.UPDATE);
    }

    private void sendTeamPacket(@NonNull Team team, byte action) {
        player.sendPacket(new net.md_5.bungee.protocol.packet.Team(
                team.getName(),
                action,
                either(TabComponent.legacyText(team.getName()), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                either(team.getPrefix(), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                either(team.getSuffix(), Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                net.md_5.bungee.protocol.packet.Team.NameTagVisibility.valueOf(team.getVisibility().name()),
                net.md_5.bungee.protocol.packet.Team.CollisionRule.valueOf(team.getCollision().name()),
                player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION ? team.getColor().getLegacyColor().ordinal() : 0,
                (byte) team.getOptions(),
                team.getPlayers().toArray(new String[0])
        ));
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (packet instanceof ScoreboardDisplay) {
            ScoreboardDisplay display = (ScoreboardDisplay) packet;
            TAB.getInstance().getFeatureManager().onDisplayObjective(player, display.getPosition(), display.getName());
        }
        if (packet instanceof ScoreboardObjective) {
            ScoreboardObjective objective = (ScoreboardObjective) packet;
            TAB.getInstance().getFeatureManager().onObjective(player, objective.getAction(), objective.getName());
        }
        if (packet instanceof net.md_5.bungee.protocol.packet.Team) {
            net.md_5.bungee.protocol.packet.Team team = (net.md_5.bungee.protocol.packet.Team) packet;
            if (team.getMode() == TeamAction.UPDATE) return;
            List<String> players = team.getPlayers() == null ? Collections.emptyList() : Lists.newArrayList(team.getPlayers());
            team.setPlayers(onTeamPacket(team.getMode(), team.getName(), players).toArray(new String[0]));
        }
    }

    @NotNull
    private Either<String, BaseComponent> either(@NonNull TabComponent text, int legacyLimit) {
        if (player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION) {
            return Either.right(player.getPlatform().transformComponent(text, player.getVersion()));
        } else {
            return Either.left(cutTo(text.toLegacyText(), legacyLimit));
        }
    }

    @Nullable
    private NumberFormat numberFormat(@Nullable TabComponent component) {
        return component == null ? null : component.toFixedFormat(baseComponent -> new NumberFormat(NumberFormat.Type.FIXED, baseComponent));
    }
}
