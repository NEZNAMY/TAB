package me.neznamy.tab.platforms.fabric.features;

import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FabricNameTagX extends BackendNameTagX {

    private boolean enabled = true;

    public FabricNameTagX() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (enabled) respawn(oldPlayer.getUUID());
        });
        // no sneaking for fabric //TODO
    }

    @Override
    public double getDistance(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        Vec3 loc1 = ((FabricTabPlayer)player1).getPlayer().position();
        Vec3 loc2 = ((FabricTabPlayer)player2).getPlayer().position();
        return Math.sqrt(Math.pow(loc1.x-loc2.x, 2) + Math.pow(loc1.z-loc2.z, 2));
    }

    @Override
    public boolean areInSameWorld(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        return ((FabricTabPlayer)player1).getPlayer().level() == ((FabricTabPlayer)player2).getPlayer().level();
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
        return ((FabricTabPlayer)player).getPlayer().isCrouching();
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
        return () -> Arrays.asList(
                new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, flags),
                new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT,
                Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion()))),
                new SynchedEntityData.DataValue<>(3, EntityDataSerializers.BOOLEAN, nameVisible),
                new SynchedEntityData.DataValue<>(15, EntityDataSerializers.BYTE, (byte)16)
        );
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
