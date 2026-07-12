package me.neznamy.tab.platforms.fand;

import io.fand.api.packet.view.ClientboundSetDisplayObjectivePacketView;
import io.fand.api.packet.view.ClientboundSetObjectivePacketView;
import io.fand.api.packet.view.ClientboundSetPlayerTeamPacketView;
import io.fand.api.scoreboard.PlayerScoreboard;
import io.fand.api.scoreboard.ScoreDisplaySlot;
import io.fand.api.scoreboard.ScoreNumberFormat;
import io.fand.api.scoreboard.ScoreRenderType;
import io.fand.api.scoreboard.ScoreboardObjective;
import io.fand.api.scoreboard.ScoreboardTeam;
import io.fand.api.scoreboard.TeamCollisionRule;
import io.fand.api.scoreboard.TeamVisibility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/** Per-viewer scoreboard implementation using Fand's public scoreboard API. */
public final class FandScoreboard extends SafeScoreboard<FandTabPlayer> {

    private static final NamedTextColor[] COLORS = {
            NamedTextColor.BLACK,
            NamedTextColor.DARK_BLUE,
            NamedTextColor.DARK_GREEN,
            NamedTextColor.DARK_AQUA,
            NamedTextColor.DARK_RED,
            NamedTextColor.DARK_PURPLE,
            NamedTextColor.GOLD,
            NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY,
            NamedTextColor.BLUE,
            NamedTextColor.GREEN,
            NamedTextColor.AQUA,
            NamedTextColor.RED,
            NamedTextColor.LIGHT_PURPLE,
            NamedTextColor.YELLOW,
            NamedTextColor.WHITE
    };

    private final PlayerScoreboard scoreboard;

    public FandScoreboard(@NotNull FandTabPlayer player) {
        super(player);
        scoreboard = player.getPlayer().scoreboard();
    }

    @Override
    public void registerObjective(@NotNull Objective objective) {
        scoreboard.registerObjective(
                objective.getName(),
                objective.getTitle().toAdventure(),
                ScoreRenderType.valueOf(objective.getHealthDisplay().name()));
        ScoreboardObjective registered = objective(objective.getName());
        registered.setNumberFormat(numberFormat(objective.getNumberFormat()));
        objective.setPlatformObjective(registered);
    }

    @Override
    public void setDisplaySlot(@NotNull Objective objective) {
        scoreboard.setDisplayedObjective(displaySlot(objective.getDisplaySlot()), objective(objective));
    }

    @Override
    public void unregisterObjective(@NotNull Objective objective) {
        scoreboard.removeObjective(objective.getName());
    }

    @Override
    public void updateObjective(@NotNull Objective objective) {
        ScoreboardObjective target = objective(objective);
        target.setDisplayName(objective.getTitle().toAdventure());
        target.setRenderType(ScoreRenderType.valueOf(objective.getHealthDisplay().name()));
        target.setNumberFormat(numberFormat(objective.getNumberFormat()));
    }

    @Override
    public void setScore(@NotNull Score score) {
        var target = objective(score.getObjective()).score(score.getHolder());
        target.setValue(score.getValue());
        target.setDisplayName(score.getDisplayName() == null ? null : score.getDisplayName().toAdventure());
        target.setNumberFormat(numberFormat(score.getNumberFormat()));
    }

    @Override
    public void removeScore(@NotNull Score score) {
        objective(score.getObjective()).resetScore(score.getHolder());
    }

    @Override
    @NotNull
    public Object createTeam(@NotNull String name) {
        return name;
    }

    @Override
    public void registerTeam(@NotNull Team team) {
        scoreboard.registerTeam(team.getName());
        ScoreboardTeam registered = team(team.getName());
        team.setPlatformTeam(registered);
        applyTeamProperties(team, registered);
        team.getPlayers().forEach(registered::addMember);
    }

    @Override
    public void unregisterTeam(@NotNull Team team) {
        scoreboard.removeTeam(team.getName());
    }

    @Override
    public void updateTeam(@NotNull Team team) {
        applyTeamProperties(team, team(team));
    }

    private void applyTeamProperties(Team source, ScoreboardTeam target) {
        target.setPrefix(source.getPrefix().toAdventure());
        target.setSuffix(source.getSuffix().toAdventure());
        target.setAllowFriendlyFire((source.getOptions() & 0x01) != 0);
        target.setSeeFriendlyInvisibles((source.getOptions() & 0x02) != 0);
        target.setNameTagVisibility(TeamVisibility.valueOf(source.getVisibility().name()));
        target.setCollisionRule(TeamCollisionRule.valueOf(source.getCollision().name()));
        target.setColor(color(source.getColor()));
    }

    private ScoreboardObjective objective(Objective objective) {
        Object platformObjective = objective.getPlatformObjective();
        if (platformObjective instanceof ScoreboardObjective value) {
            return value;
        }
        ScoreboardObjective value = objective(objective.getName());
        objective.setPlatformObjective(value);
        return value;
    }

    private ScoreboardObjective objective(String name) {
        return scoreboard.objective(name)
                .orElseThrow(() -> new IllegalStateException("Missing Fand scoreboard objective: " + name));
    }

    private ScoreboardTeam team(Team team) {
        Object platformTeam = team.getPlatformTeam();
        if (platformTeam instanceof ScoreboardTeam value) {
            return value;
        }
        ScoreboardTeam value = team(team.getName());
        team.setPlatformTeam(value);
        return value;
    }

    private ScoreboardTeam team(String name) {
        return scoreboard.team(name)
                .orElseThrow(() -> new IllegalStateException("Missing Fand scoreboard team: " + name));
    }

    private static ScoreDisplaySlot displaySlot(DisplaySlot slot) {
        return slot == DisplaySlot.PLAYER_LIST ? ScoreDisplaySlot.LIST : ScoreDisplaySlot.valueOf(slot.name());
    }

    private static ScoreNumberFormat numberFormat(TabComponent component) {
        return component == null ? ScoreNumberFormat.DEFAULT : ScoreNumberFormat.fixed(component.toAdventure());
    }

    private static NamedTextColor color(EnumChatFormat color) {
        return color.isColor() ? COLORS[color.ordinal()] : null;
    }

    void observeDisplayObjective(@NotNull ClientboundSetDisplayObjectivePacketView packet) {
        String objectiveName = packet.objectiveName();
        ScoreDisplaySlot slot = packet.value("slot", ScoreDisplaySlot.class);
        notifyDisplayObjective(slot, objectiveName);
    }

    private void notifyDisplayObjective(ScoreDisplaySlot slot, String objectiveName) {
        TAB tab = TAB.getInstance();
        if (tab == null || tab.isPluginDisabled() || tab.getPlayer(player.getUniqueId()) != player) {
            return;
        }
        tab.getFeatureManager().onDisplayObjective(player, slot.ordinal(), objectiveName);
    }

    void observeObjective(@NotNull ClientboundSetObjectivePacketView packet) {
        TAB.getInstance().getFeatureManager().onObjective(player, packet.method(), packet.objectiveName());
    }

    @NotNull
    ClientboundSetPlayerTeamPacketView rewriteTeam(@NotNull ClientboundSetPlayerTeamPacketView packet) {
        if (packet.method() == TeamAction.UPDATE) {
            return packet;
        }
        Collection<?> rawPlayers = packet.value("players", Collection.class);
        List<String> players = new ArrayList<>(rawPlayers.size());
        for (Object entry : rawPlayers) {
            if (!(entry instanceof String playerName)) {
                return packet;
            }
            players.add(playerName);
        }
        Collection<String> replacement = onTeamPacket(packet.method(), packet.name(), players);
        if (players.equals(replacement)) {
            return packet;
        }
        return packet.with(
                "players",
                List.copyOf(replacement),
                ClientboundSetPlayerTeamPacketView.class);
    }

}
