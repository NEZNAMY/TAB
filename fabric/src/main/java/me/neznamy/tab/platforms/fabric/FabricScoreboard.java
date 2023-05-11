package me.neznamy.tab.platforms.fabric;

import java.util.Collection;
import java.util.Objects;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.PlatformScoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;

public class FabricScoreboard extends PlatformScoreboard<FabricTabPlayer> {

    private final @NotNull Scoreboard dummyScoreboard = new Scoreboard();
    private final @NotNull Component EMPTY_COMPONENT = Objects.requireNonNull(Component.Serializer.fromJson(IChatBaseComponent.EMPTY_COMPONENT));

    public FabricScoreboard(FabricTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(@NonNull DisplaySlot slot, @NonNull String objective) {
        player.sendPacket(
                new ClientboundSetDisplayObjectivePacket(
                        slot.ordinal(),
                        new Objective(
                                dummyScoreboard,
                                objective,
                                ObjectiveCriteria.DUMMY,
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
                            ObjectiveCriteria.DUMMY,
                            FabricTAB.getInstance().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
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
                                ObjectiveCriteria.DUMMY,
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
                            ObjectiveCriteria.DUMMY,
                            FabricTAB.getInstance().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                            hearts ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER
                        ),
                        2
                )
        );
    }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, @NonNull Collection<String> players, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(visibility.name()));
        team.setPlayerPrefix(FabricTAB.getInstance().toComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        team.setPlayerSuffix(FabricTAB.getInstance().toComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        team.getPlayers().addAll(players);
        player.sendPacket(FabricMultiVersion.registerTeam.apply(team));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(FabricMultiVersion.unregisterTeam.apply(new PlayerTeam(dummyScoreboard, name)));
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(visibility.name()));
        team.setPlayerPrefix(FabricTAB.getInstance().toComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        team.setPlayerSuffix(FabricTAB.getInstance().toComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        player.sendPacket(FabricMultiVersion.updateTeam.apply(team));
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
