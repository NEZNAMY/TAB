package me.neznamy.tab.platforms.fabric;

import java.util.Collection;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;

public class FabricScoreboard extends Scoreboard<FabricTabPlayer> {

    @NotNull
    private static final net.minecraft.world.scores.Scoreboard dummyScoreboard = new net.minecraft.world.scores.Scoreboard();

    public FabricScoreboard(FabricTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.sendPacket(
                new ClientboundSetDisplayObjectivePacket(
                        net.minecraft.world.scores.DisplaySlot.values()[slot.ordinal()],
                        new Objective(
                                dummyScoreboard,
                                objective,
                                ObjectiveCriteria.DUMMY,
                                Component.empty(),
                                ObjectiveCriteria.RenderType.INTEGER
                        )
                )
        );
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        player.sendPacket(
                new ClientboundSetObjectivePacket(
                        new Objective(
                                dummyScoreboard,
                                objectiveName,
                                ObjectiveCriteria.DUMMY,
                                player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                                ObjectiveCriteria.RenderType.valueOf(display.name())
                        ),
                        0
                )
        );
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPacket(
                new ClientboundSetObjectivePacket(
                        new Objective(
                                dummyScoreboard,
                                objectiveName,
                                ObjectiveCriteria.DUMMY,
                                Component.empty(),
                                ObjectiveCriteria.RenderType.INTEGER
                        ),
                        1
                )
        );
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        player.sendPacket(
                new ClientboundSetObjectivePacket(
                        new Objective(
                                dummyScoreboard,
                                objectiveName,
                                ObjectiveCriteria.DUMMY,
                                player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                                ObjectiveCriteria.RenderType.valueOf(display.name())
                        ),
                        2
                )
        );
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(visibility.name()));
        team.setPlayerPrefix(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        team.setPlayerSuffix(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        team.getPlayers().addAll(players);
        player.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket(new PlayerTeam(dummyScoreboard, name)));
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setSeeFriendlyInvisibles((options & 0x02) != 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(visibility.name()));
        team.setPlayerPrefix(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        team.setPlayerSuffix(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        player.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, playerName, score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, playerName, 0));
    }
}
