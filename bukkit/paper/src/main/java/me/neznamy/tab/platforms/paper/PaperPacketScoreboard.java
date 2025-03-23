package me.neznamy.tab.platforms.paper;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

/**
 * Scoreboard implementation using direct mojang-mapped code.
 */
@SuppressWarnings("unused") // Used via reflection
public class PaperPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    private static final ChatFormatting[] formats = ChatFormatting.values();
    private static final net.minecraft.world.scores.Team.CollisionRule[] collisions = net.minecraft.world.scores.Team.CollisionRule.values();
    private static final net.minecraft.world.scores.Team.Visibility[] visibilities = net.minecraft.world.scores.Team.Visibility.values();
    private static final Scoreboard dummyScoreboard = new Scoreboard();
    private static final Field method;
    private static final Field players;

    static {
        try {
            method = ReflectionUtils.setAccessible(ClientboundSetPlayerTeamPacket.class.getDeclaredField("method"));
            players = ReflectionUtils.setAccessible(ClientboundSetPlayerTeamPacket.class.getDeclaredField("players"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public PaperPacketScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        net.minecraft.world.scores.Objective obj = new net.minecraft.world.scores.Objective(
                dummyScoreboard,
                objective.getName(),
                ObjectiveCriteria.DUMMY,
                objective.getTitle().convert(),
                ObjectiveCriteria.RenderType.values()[objective.getHealthDisplay().ordinal()],
                false,
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().toFixedFormat(FixedFormat::new)
        );
        objective.setPlatformObjective(obj);
        sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.REGISTER));
        sendPacket(new ClientboundSetDisplayObjectivePacket(net.minecraft.world.scores.DisplaySlot.values()[objective.getDisplaySlot().ordinal()], obj));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sendPacket(new ClientboundSetObjectivePacket((net.minecraft.world.scores.Objective) objective.getPlatformObjective(), ObjectiveAction.UNREGISTER));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        net.minecraft.world.scores.Objective obj = (net.minecraft.world.scores.Objective) objective.getPlatformObjective();
        obj.setDisplayName(objective.getTitle().convert());
        obj.setRenderType(ObjectiveCriteria.RenderType.values()[objective.getHealthDisplay().ordinal()]);
        sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void setScore(@NonNull Score score) {
        sendPacket(
                new ClientboundSetScorePacket(
                        score.getHolder(),
                        score.getObjective().getName(),
                        score.getValue(),
                        Optional.ofNullable(score.getDisplayName() == null ? null : score.getDisplayName().convert()),
                        Optional.ofNullable(score.getNumberFormat() == null ? null : score.getNumberFormat().toFixedFormat(FixedFormat::new))
                )
        );
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sendPacket(new ClientboundResetScorePacket(score.getHolder(), score.getObjective().getName()));
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new PlayerTeam(dummyScoreboard, name);
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        updateTeamProperties(team);
        PlayerTeam t = (PlayerTeam) team.getPlatformTeam();
        t.getPlayers().addAll(team.getPlayers());
        sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(t, true));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket((PlayerTeam) team.getPlatformTeam()));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        updateTeamProperties(team);
        sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket((PlayerTeam) team.getPlatformTeam(), false));
    }

    private void updateTeamProperties(@NonNull Team team) {
        PlayerTeam t = (PlayerTeam) team.getPlatformTeam();
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
        t.setColor(formats[team.getColor().getLegacyColor().ordinal()]);
        t.setCollisionRule(collisions[team.getCollision().ordinal()]);
        t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        t.setPlayerPrefix(team.getPrefix().convert());
        t.setPlayerSuffix(team.getSuffix().convert());
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (isAntiOverrideScoreboard()) {
            if (packet instanceof ClientboundSetDisplayObjectivePacket display) {
                TAB.getInstance().getFeatureManager().onDisplayObjective(player, display.getSlot().ordinal(), display.getObjectiveName());
            }
            if (packet instanceof ClientboundSetObjectivePacket objective) {
                TAB.getInstance().getFeatureManager().onObjective(player, objective.getMethod(), objective.getObjectiveName());
            }
        }
        if (isAntiOverrideTeams() && packet instanceof ClientboundSetPlayerTeamPacket team) {
            int action = method.getInt(team);
            if (action == TeamAction.UPDATE) return;
            players.set(team, onTeamPacket(action, team.getName(), team.getPlayers() == null ? Collections.emptyList() : team.getPlayers()));
        }
    }
    
    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().connection.sendPacket(packet);
    }
}
