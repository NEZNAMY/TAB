package me.neznamy.tab.platforms.fabric;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.Scoreboard;
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

public class FabricScoreboard extends Scoreboard<FabricTabPlayer> {

    private static final net.minecraft.world.scores.Scoreboard dummyScoreboard = new net.minecraft.world.scores.Scoreboard();
    private static final ObjectiveCriteria dummyCriteria = ObjectiveCriteria.DUMMY;
    @NonNull private static final Component EMPTY_COMPONENT = Objects.requireNonNull(Component.Serializer.fromJson("{\"text\":\"\"}"));

    public FabricScoreboard(FabricTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot, @NonNull String objective) {
        player.sendPacket(
                new ClientboundSetDisplayObjectivePacket(
                        slot.ordinal(),
                        new Objective(
                                dummyScoreboard,
                                objective,
                                dummyCriteria,
                                EMPTY_COMPONENT,
                                ObjectiveCriteria.RenderType.INTEGER
                        )
                )
        );
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(
                new ClientboundSetObjectivePacket(
                        new Objective(
                            dummyScoreboard,
                            objectiveName,
                            dummyCriteria,
                            FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                            hearts ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER
                        ),
                        0
                )
        );
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(
                new ClientboundSetObjectivePacket(
                        new Objective(
                                dummyScoreboard,
                                objectiveName,
                                dummyCriteria,
                                EMPTY_COMPONENT,
                                ObjectiveCriteria.RenderType.INTEGER
                        ),
                        1
                )
        );
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(
                new ClientboundSetObjectivePacket(
                        new Objective(
                            dummyScoreboard,
                            objectiveName,
                            dummyCriteria,
                            FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                            hearts ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER
                        ),
                        2
                )
        );
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        if (collision != null)
            team.setCollisionRule(Team.CollisionRule.valueOf(collision.toUpperCase(Locale.US)));
        if (visibility != null)
            team.setNameTagVisibility(Team.Visibility.valueOf(visibility.toUpperCase(Locale.US)));
        if (prefix != null)
            team.setPlayerPrefix(FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        if (suffix != null)
            team.setPlayerSuffix(FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        team.getPlayers().addAll(players);
        player.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket(new PlayerTeam(dummyScoreboard, name)));
    }

    @Override
    public void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        if (collision != null)
            team.setCollisionRule(Team.CollisionRule.valueOf(collision.toUpperCase(Locale.US)));
        if (visibility != null)
            team.setNameTagVisibility(Team.Visibility.valueOf(visibility.toUpperCase(Locale.US)));
        if (prefix != null)
            team.setPlayerPrefix(FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        if (suffix != null)
            team.setPlayerSuffix(FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        player.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        player.sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, playerName, score));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        player.sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, playerName, 0));
    }
}
