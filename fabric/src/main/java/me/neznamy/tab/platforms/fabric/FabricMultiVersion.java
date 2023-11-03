package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import me.neznamy.tab.platforms.fabric.features.FabricNameTagX;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Class that contains (almost) all code that needs to be changed
 * to set compatibility with any MC version that fabric supports (1.14.4+).
 */
public class FabricMultiVersion {

    private static final int armorStandFlagsPosition = getArmorStandFlagsPosition();

    private static int getArmorStandFlagsPosition() {
        int minorVersion = ProtocolVersion.fromFriendlyName(SharedConstants.getCurrentVersion().getName()).getMinorVersion();
        if (minorVersion >= 17) {
            //1.17.x, 1.18.x, 1.19.x, 1.20.x
            return 15;
        } else if (minorVersion >= 15) {
            //1.15.x, 1.16.x
            return 14;
        } else {
            //1.14.x
            return 13;
        }
    }

    public static String getWorldName(Level level) {
        // 1.15.2-
        // return level.getLevelData().getLevelName() + level.dimension.getType().getFileSuffix();

        // 1.16+
        String path = level.dimension().location().getPath();
        String dimensionSuffix = switch (path) {
            case "overworld" -> ""; // No suffix for overworld
            case "the_nether" -> "_nether";
            default -> "_" + path; // End + default behavior for other dimensions created by mods
        };
        return ((ServerLevelData)level.getLevelData()).getLevelName() + dimensionSuffix;
    }
    
    public static void sendSystemMessage(CommandSourceStack source, Component message) {
        // 1.19.4-
        // source.sendSuccess(message, false);

        // 1.19.1+
        source.sendSystemMessage(message);
    }

    public static boolean isSneaking(ServerPlayer player) {
        // 1.14.4-
        // return player.isSneaking();

        // 1.15+
        return player.isCrouching();
    }

    public static void registerNameTagXEvents(FabricNameTagX nameTagX) {
        // 1.16+
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (nameTagX.enabled) nameTagX.respawn(oldPlayer.getUUID());
        });
        // TODO sneaking
    }

    public static void registerEntityEvents(BiConsumer<UUID, ServerPlayer> respawnAction, BiConsumer<UUID, String> worldChangeAction) {
        // 1.16+
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> {
                    respawnAction.accept(newPlayer.getUUID(), newPlayer);
                    // respawning from death & taking end portal in the end do not call world change event
                    worldChangeAction.accept(newPlayer.getUUID(), getWorldName(newPlayer.level));
                });
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                (player, origin, destination) -> worldChangeAction.accept(player.getUUID(), getWorldName(destination)));
    }

    public static ClientboundTabListPacket setHeaderFooter(Component header, Component footer) {
        // 1.16.5-
        /*ClientboundTabListPacket packet = new ClientboundTabListPacket();
        packet.header = header;
        packet.footer = footer;
        return packet;*/

        // 1.17+
        return new ClientboundTabListPacket(header, footer);
    }

    public static ClientboundSetPlayerTeamPacket createTeam(PlayerTeam team) {
        // 1.16.5-
        // return new ClientboundSetPlayerTeamPacket(team, 0);

        // 1.17+
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
    }

    public static ClientboundSetPlayerTeamPacket removeTeam(PlayerTeam team) {
        // 1.16.5-
        // return new ClientboundSetPlayerTeamPacket(team, 1);

        // 1.17+
        return ClientboundSetPlayerTeamPacket.createRemovePacket(team);
    }

    public static ClientboundSetPlayerTeamPacket updateTeam(PlayerTeam team) {
        // 1.16.5-
        // return new ClientboundSetPlayerTeamPacket(team, 2);

        // 1.17+
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
    }

    public static void setProgress(ServerBossEvent bar, float progress) {
        // 1.16.5-
        // bar.setPercent(progress);

        // 1.17+
        bar.setProgress(progress);
    }

    public static int[] getDestroyedEntities(Object destroyPacket) {
        // 1.16.5-
        // return ((ClientboundRemoveEntitiesPacket) destroyPacket).getEntityIds();

        // 1.17
        // return new int[]{((ClientboundRemoveEntityPacket)destroyPacket).getEntityId()};

        // 1.17.1+
        return ((ClientboundRemoveEntitiesPacket) destroyPacket).getEntityIds().toIntArray();
    }

    public static void destroyEntities(FabricTabPlayer player, int... entities) {
        // 1.17
        /*for (int i : entities) {
            player.sendPacket(new ClientboundRemoveEntityPacket(i));
        }*/

        // every other version
        player.sendPacket(new ClientboundRemoveEntitiesPacket(entities));
    }

    public static boolean isEntityDestroyPacket(Object packet) {
        // 1.17
        // return packet instanceof ClientboundRemoveEntityPacket;

        // every other version
        return packet instanceof ClientboundRemoveEntitiesPacket;
    }

    public static void registerCommand() {
        // 1.18.2-
        // net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, $) -> new FabricTabCommand().onRegisterCommands(dispatcher));

        // 1.19+
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> new FabricTabCommand().onRegisterCommands(dispatcher));
    }

    public static void sendMessage(FabricTabPlayer player, Component message) {
        // 1.15.2-
        // player.getPlayer().sendMessage(message);

        // 1.16 - 1.18.2
        // player.getPlayer().sendMessage(message, new UUID(0, 0));

        // 1.19+
        player.getPlayer().sendSystemMessage(message);
    }

    public static ClientboundAddEntityPacket addEntity(int entityId, @NotNull UUID id, @NotNull Object entityType,
                                                       @NotNull Location location, @NotNull EntityData data) {
        // 1.18.2-
        // return new ClientboundAddEntityPacket(entityId, id, location.getX(), location.getY(), location.getZ(), 0, 0, (EntityType<?>) entityType, 0, Vec3.ZERO);

        // 1.19+
        return new ClientboundAddEntityPacket(entityId, id, location.getX(), location.getY(), location.getZ(), 0, 0, (EntityType<?>) entityType, 0, Vec3.ZERO, 0);
    }

    public static boolean isPlayerInfo(Object packet) {
        // 1.19.2-
        // return packet instanceof ClientboundPlayerInfoPacket;

        // 1.19.3+
        return packet instanceof ClientboundPlayerInfoUpdatePacket;
    }

    public static Packet<?> build(FabricTabList.Action action, FabricTabList.Builder entry) {
        // 1.16.5-
        /*ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoPacket().new PlayerUpdate(
                entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName()
        ));
        return packet;*/

        // 1.17 - 1.18.2
        /*ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoPacket.PlayerUpdate(
                entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName()
        ));
        return packet;*/

        // 1.19 - 1.19.2
        /*ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoPacket.PlayerUpdate(
                entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName(), null
        ));
        return packet;*/

        // 1.19.3+
        if (action == FabricTabList.Action.REMOVE_PLAYER) {
            return new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry.getId()));
        }
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = action == FabricTabList.Action.ADD_PLAYER ?
                EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class) :
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.valueOf(action.name()));
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoUpdatePacket.Entry(
                entry.getId(),
                entry.createProfile(),
                true,
                entry.getLatency(),
                GameType.byId(entry.getGameMode()),
                entry.getDisplayName(),
                null
        ));
        return packet;
    }

    public static EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible) {
        // 1.19.2-
        /*SynchedEntityData data = new SynchedEntityData(null);
        data.define(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), flags);
        data.define(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT),
                Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion())));
        data.define(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), nameVisible);
        data.define(new EntityDataAccessor<>(15, EntityDataSerializers.BYTE), (byte)16);
        return () -> data;*/

        // 1.19.3+
        return () -> java.util.Arrays.asList(
                new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, flags),
                new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT,
                        Optional.of(((FabricTabPlayer)viewer).getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), viewer.getVersion()))),
                new SynchedEntityData.DataValue<>(3, EntityDataSerializers.BOOLEAN, nameVisible),
                new SynchedEntityData.DataValue<>(armorStandFlagsPosition, EntityDataSerializers.BYTE, (byte)16)
        );
    }

    @SuppressWarnings("unchecked")
    public static ClientboundSetEntityDataPacket newEntityMetadata(int entityId, EntityData data) {
        // 1.19.2-
        // return new ClientboundSetEntityDataPacket(entityId, (SynchedEntityData) data.build(), true);

        // 1.19.3+
        return new ClientboundSetEntityDataPacket(entityId, (List<SynchedEntityData.DataValue<?>>) data.build());
    }

    public static boolean isBundlePacket(Object packet) {
        // 1.19.4+
        return packet instanceof ClientboundBundlePacket;
    }

    public static Iterable<Packet<ClientGamePacketListener>> getPackets(Object bundlePacket) {
        // 1.19.4+
        return ((ClientboundBundlePacket)bundlePacket).subPackets();
    }

    public static ClientboundSetDisplayObjectivePacket newDisplayObjective(int slot, Objective objective) {
        // 1.20.1-
        // return new ClientboundSetDisplayObjectivePacket(slot, objective);

        // 1.20.2+
        return new ClientboundSetDisplayObjectivePacket(net.minecraft.world.scores.DisplaySlot.values()[slot], objective);
    }

    public static int getSlot(ClientboundSetDisplayObjectivePacket packet) {
        // 1.20.1-
        // return packet.getSlot();

        // 1.20.2+
        return packet.getSlot().ordinal();
    }

    public static int getPing(FabricTabPlayer player) {
        // 1.20.1-
        // return player.getPlayer().latency;

        // 1.20.2+
        return player.getPlayer().connection.latency();
    }

    public static TabList.Skin propertyToSkin(Property property) {
        // 1.20.1-
        // return new TabList.Skin(property.getValue(), property.getSignature());

        // 1.20.2+
        return new TabList.Skin(property.value(), property.signature());
    }
}
