package me.neznamy.tab.platforms.fabric;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Scoreboard implementation for Fabric using packets.
 */
public class FabricScoreboard extends Scoreboard<FabricTabPlayer, Component> {

    private static final net.minecraft.world.scores.Scoreboard dummyScoreboard = new net.minecraft.world.scores.Scoreboard();

    private final Map<String, Objective> objectives = new HashMap<>();

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
    public void setDisplaySlot0(int slot, @NonNull String objective) {
        player.sendPacket(FabricMultiVersion.setDisplaySlot(slot, objectives.get(objective)));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                   @Nullable Component numberFormat) {
        Objective obj = FabricMultiVersion.newObjective(
                objectiveName,
                toComponent(title),
                RenderType.values()[display],
                numberFormat
        );
        objectives.put(objectiveName, obj);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.REGISTER));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(new ClientboundSetObjectivePacket(objectives.remove(objectiveName), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                 @Nullable Component numberFormat) {
        Objective obj = objectives.get(objectiveName);
        obj.setDisplayName(toComponent(title));
        obj.setRenderType(RenderType.values()[display]);
        player.sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                              @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                              @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(color.name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(visibility.name()));
        team.setPlayerPrefix(toComponent(prefix));
        team.setPlayerSuffix(toComponent(suffix));
        team.getPlayers().addAll(players);
        player.sendPacket(FabricMultiVersion.registerTeam(team));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(FabricMultiVersion.unregisterTeam(new PlayerTeam(dummyScoreboard, name)));
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                            @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                            int options, @NonNull EnumChatFormat color) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setSeeFriendlyInvisibles((options & 0x02) != 0);
        team.setColor(ChatFormatting.valueOf(color.name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(visibility.name()));
        team.setPlayerPrefix(toComponent(prefix));
        team.setPlayerSuffix(toComponent(suffix));
        player.sendPacket(FabricMultiVersion.updateTeam(team));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score,
                          @Nullable Component displayName, @Nullable Component numberFormat) {
        player.sendPacket(FabricMultiVersion.setScore(objective, scoreHolder, score, displayName, numberFormat));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        player.sendPacket(FabricMultiVersion.removeScore(objective, scoreHolder));
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (packet instanceof ClientboundSetDisplayObjectivePacket display) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(player, FabricMultiVersion.getDisplaySlot(display), display.objectiveName);
        }
        if (packet instanceof ClientboundSetObjectivePacket objective) {
            TAB.getInstance().getFeatureManager().onObjective(player, objective.method, objective.objectiveName);
        }
        if (isAntiOverrideTeams() && FabricMultiVersion.isTeamPacket((Packet<?>) packet)) {
            if (TAB.getInstance().getNameTagManager() == null) return;
            int action = ReflectionUtils.getInstanceFields(packet.getClass(), int.class).get(0).getInt(packet);
            if (action == 1 || action == 2 || action == 4) return;
            Field playersField = ReflectionUtils.getFields(packet.getClass(), Collection.class).get(0);
            Collection<String> players = (Collection<String>) playersField.get(packet);
            String teamName = String.valueOf(ReflectionUtils.getFields(packet.getClass(), String.class).get(0).get(packet));
            if (players == null) return;
            //creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
            Collection<String> newList = new ArrayList<>();
            for (String entry : players) {
                TabPlayer p = getPlayer(entry);
                if (p == null) {
                    newList.add(entry);
                    continue;
                }
                Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
                String expectedTeam = sorting.getShortTeamName(p);
                if (expectedTeam == null) {
                    newList.add(entry);
                    continue;
                }
                if (!TAB.getInstance().getNameTagManager().getDisableChecker().isDisabledPlayer(p) &&
                        !TAB.getInstance().getNameTagManager().hasTeamHandlingPaused(p) && !teamName.equals(expectedTeam)) {
                    logTeamOverride(teamName, p.getName(), expectedTeam);
                } else {
                    newList.add(entry);
                }
            }
            playersField.set(packet, newList);
        }
    }

    @NonNull
    private Component toComponent(@NonNull String string) {
        return TabComponent.optimized(string).convert(player.getVersion());
    }
}
