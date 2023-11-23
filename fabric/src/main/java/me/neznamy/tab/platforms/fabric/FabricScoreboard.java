package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FabricScoreboard extends Scoreboard<FabricTabPlayer> {

    @NotNull
    private static final net.minecraft.world.scores.Scoreboard dummyScoreboard = new net.minecraft.world.scores.Scoreboard();

    private final Map<String, Objective> objectives = new HashMap<>();

    public FabricScoreboard(FabricTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(int slot, @NotNull String objective) {
        player.sendPacket(FabricTAB.getVersion().setDisplaySlot(slot, objectives.get(objective)));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable IChatBaseComponent numberFormat) {
        Objective obj = FabricTAB.getVersion().newObjective(
                objectiveName,
                toComponent(title),
                RenderType.values()[display],
                numberFormat == null ? null : toComponent(numberFormat)
        );
        objectives.put(objectiveName, obj);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.REGISTER));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPacket(new ClientboundSetObjectivePacket(objectives.remove(objectiveName), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable IChatBaseComponent numberFormat) {
        Objective obj = objectives.get(objectiveName);
        obj.setDisplayName(toComponent(title));
        obj.setRenderType(RenderType.values()[display]);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
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
        team.setPlayerPrefix(toComponent(prefix));
        team.setPlayerSuffix(toComponent(suffix));
        team.getPlayers().addAll(players);
        player.sendPacket(FabricTAB.getVersion().registerTeam(team));
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.sendPacket(FabricTAB.getVersion().unregisterTeam(new PlayerTeam(dummyScoreboard, name)));
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
        team.setPlayerPrefix(toComponent(prefix));
        team.setPlayerSuffix(toComponent(suffix));
        player.sendPacket(FabricTAB.getVersion().updateTeam(team));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable IChatBaseComponent displayName, @Nullable IChatBaseComponent numberFormat) {
        player.sendPacket(FabricTAB.getVersion().setScore(
                objective,
                scoreHolder,
                score,
                displayName == null ? null : toComponent(displayName),
                numberFormat == null ? null : toComponent(numberFormat)
        ));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        player.sendPacket(FabricTAB.getVersion().removeScore(objective, scoreHolder));
    }

    @NotNull
    private Component toComponent(@NotNull String string) {
        return toComponent(IChatBaseComponent.optimizedComponent(string));
    }

    @NotNull
    private Component toComponent(@NotNull IChatBaseComponent component) {
        return player.getPlatform().toComponent(component, player.getVersion());
    }
}
