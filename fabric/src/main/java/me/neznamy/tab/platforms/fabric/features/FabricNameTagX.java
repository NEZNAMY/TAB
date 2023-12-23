package me.neznamy.tab.platforms.fabric.features;

import me.neznamy.tab.platforms.fabric.FabricMultiVersion;
import me.neznamy.tab.platforms.fabric.FabricTAB;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Unlimited nametag mode implementation for Fabric.
 */
public class FabricNameTagX extends BackendNameTagX {

    /** Flag tracking whether this instance is still running or not */
    private boolean enabled = true;

    /**
     * Constructs new instance and registers event listener.
     */
    public FabricNameTagX() {
        if (FabricTAB.supportsEntityEvents()) {
            // Added in 1.16
            ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
                if (enabled) respawn(oldPlayer.getUUID());
            });
        } // TODO else
        // TODO sneaking
    }

    @Override
    public double getDistance(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        Vec3 loc1 = ((FabricTabPlayer)player1).getPlayer().position();
        Vec3 loc2 = ((FabricTabPlayer)player2).getPlayer().position();
        return Math.sqrt(Math.pow(loc1.x-loc2.x, 2) + Math.pow(loc1.z-loc2.z, 2));
    }

    @Override
    public boolean areInSameWorld(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        return FabricMultiVersion.getLevel.apply(((FabricTabPlayer)player1).getPlayer()) ==
                FabricMultiVersion.getLevel.apply(((FabricTabPlayer)player2).getPlayer());
    }

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return true;
    }

    @Override
    public void unregisterListener() {
        enabled = false;
    }

    @Override
    @NotNull
    public List<Integer> getPassengers(@NotNull Object vehicle) {
        return ((Entity)vehicle).getPassengers().stream().map(Entity::getId).collect(Collectors.toList());
    }

    @Override
    @Nullable
    public Object getVehicle(@NotNull TabPlayer player) {
        return ((FabricTabPlayer)player).getPlayer().getVehicle();
    }

    @Override
    public int getEntityId(@NotNull Object entity) {
        return ((Entity)entity).getId();
    }

    @Override
    @NotNull
    public String getEntityType(@NotNull Object entity) {
        return ((Entity)entity).getType().toString(); // TODO test/fix
    }

    @Override
    public boolean isSneaking(@NotNull TabPlayer player) {
        return FabricMultiVersion.isSneaking.apply(((FabricTabPlayer)player).getPlayer());
    }

    @Override
    public boolean isSwimming(@NotNull TabPlayer player) {
        return ((FabricTabPlayer)player).getPlayer().isSwimming();
    }

    @Override
    public boolean isGliding(@NotNull TabPlayer player) {
        return false; // TODO
    }

    @Override
    public boolean isSleeping(@NotNull TabPlayer player) {
        return ((FabricTabPlayer)player).getPlayer().isSleeping();
    }

    @Override
    @NotNull
    public Object getArmorStandType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public double getX(@NotNull TabPlayer player) {
        return ((FabricTabPlayer)player).getPlayer().position().x;
    }

    @Override
    public double getY(@NotNull Object entity) {
        return ((Entity)entity).position().y;
    }

    @Override
    public double getZ(@NotNull TabPlayer player) {
        return ((FabricTabPlayer)player).getPlayer().position().z;
    }

    @Override
    @NotNull
    public EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible) {
        return FabricMultiVersion.createDataWatcher.apply(viewer, flags, displayName, nameVisible);
    }

    @Override
    public void runInEntityScheduler(@NotNull Object entity, @NotNull Runnable task) {
        task.run();
    }

    @Override
    public boolean isDead(@NotNull TabPlayer player) {
        return !((FabricTabPlayer)player).getPlayer().isAlive();
    }
}
