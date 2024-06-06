package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.NumberFormat;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.ScoreboardScoreReset;
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
        player.sendPacket(new ScoreboardObjective(
                objective.getName(),
                either(objective.getTitle()),
                ScoreboardObjective.HealthDisplay.values()[objective.getHealthDisplay().ordinal()],
                (byte) ObjectiveAction.REGISTER,
                numberFormat(objective.getNumberFormat())
        ));
        player.sendPacket(new ScoreboardDisplay(objective.getDisplaySlot().ordinal(), objective.getName()));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        player.sendPacket(new ScoreboardObjective(
                objective.getName(),
                either(""), // Empty value instead of null to prevent NPE kick on 1.7
                null,
                (byte) ObjectiveAction.UNREGISTER,
                null
        ));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        player.sendPacket(new ScoreboardObjective(
                objective.getName(),
                either(objective.getTitle()),
                ScoreboardObjective.HealthDisplay.values()[objective.getHealthDisplay().ordinal()],
                (byte) ObjectiveAction.UPDATE,
                numberFormat(objective.getNumberFormat())
        ));
    }

    @Override
    public void setScore(@NonNull Score score) {
        player.sendPacket(new ScoreboardScore(
                score.getHolder(),
                (byte) ScoreAction.CHANGE,
                score.getObjective(),
                score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().convert(player.getVersion()),
                numberFormat(score.getNumberFormat())
        ));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
            player.sendPacket(new ScoreboardScoreReset(score.getHolder(), score.getObjective()));
        } else {
            player.sendPacket(new ScoreboardScore(score.getHolder(), (byte) ScoreAction.REMOVE, score.getObjective(), 0, null, null));
        }
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        player.sendPacket(new net.md_5.bungee.protocol.packet.Team(
                team.getName(),
                (byte) TeamAction.CREATE,
                either(team.getName()),
                either(team.getPrefix()),
                either(team.getSuffix()),
                team.getVisibility().toString(),
                team.getCollision().toString(),
                player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION ? team.getColor().ordinal() : 0,
                (byte) team.getOptions(),
                team.getPlayers().toArray(new String[0])
        ));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        player.sendPacket(new net.md_5.bungee.protocol.packet.Team(team.getName()));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        player.sendPacket(new net.md_5.bungee.protocol.packet.Team(
                team.getName(),
                (byte) TeamAction.UPDATE,
                either(team.getName()),
                either(team.getPrefix()),
                either(team.getSuffix()),
                team.getVisibility().toString(),
                team.getCollision().toString(),
                player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION ? team.getColor().ordinal() : 0,
                (byte) team.getOptions(),
                null
        ));
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (isAntiOverrideScoreboard()) {
            if (packet instanceof ScoreboardDisplay) {
                ScoreboardDisplay display = (ScoreboardDisplay) packet;
                TAB.getInstance().getFeatureManager().onDisplayObjective(player, display.getPosition(), display.getName());
            }
            if (packet instanceof ScoreboardObjective) {
                ScoreboardObjective objective = (ScoreboardObjective) packet;
                TAB.getInstance().getFeatureManager().onObjective(player, objective.getAction(), objective.getName());
            }
        }
        if (isAntiOverrideTeams() && packet instanceof net.md_5.bungee.protocol.packet.Team) {
            net.md_5.bungee.protocol.packet.Team team = (net.md_5.bungee.protocol.packet.Team) packet;
            if (team.getMode() == TeamAction.UPDATE) return;
            List<String> players = team.getPlayers() == null ? Collections.emptyList() : Lists.newArrayList(team.getPlayers());
            team.setPlayers(onTeamPacket(team.getMode(), team.getName(), players).toArray(new String[0]));
        }
    }

    private Either<String, BaseComponent> either(@NonNull String text) {
        if (player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION) {
            return Either.right(TabComponent.optimized(text).convert(player.getVersion()));
        } else {
            return Either.left(text);
        }
    }

    @Nullable
    private NumberFormat numberFormat(@Nullable TabComponent component) {
        return component == null ? null : new NumberFormat(NumberFormat.Type.FIXED, component.convert(player.getVersion()));
    }
}
