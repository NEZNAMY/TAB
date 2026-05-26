package me.neznamy.tab.platforms.bukkit.paper_1_21_11;

import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;

/**
 * Scoreboard override for Canvas (Folia fork) that implements scoreboards with
 * new checks in NMS setters since 26.1.2.
 */
public class CanvasPacketScoreboard extends NMSPacketScoreboard {

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public CanvasPacketScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    /**
     * Updates the objective by creating a new one instead of updating the existing one,
     * which would require sync access on Canvas.
     *
     * @param   objective
     *          Objective to update
     */
    @Override
    public void updateObjective(@NonNull Objective objective) {
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
        sendPacket(new ClientboundSetObjectivePacket(obj, ObjectiveAction.UPDATE));
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        player.getPlatform().runSyncGlobal(() -> super.registerTeam(team));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        // This one too, to avoid register+unregister causing unregister to run first
        player.getPlatform().runSyncGlobal(() -> super.unregisterTeam(team));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        player.getPlatform().runSyncGlobal(() -> super.updateTeam(team));
    }
}
