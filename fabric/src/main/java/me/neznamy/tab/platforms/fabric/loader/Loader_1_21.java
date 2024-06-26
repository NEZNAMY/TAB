package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.FabricScoreboard;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Method loader compiled using Minecraft 1.21.
 */
@SuppressWarnings({
        "unchecked", // Java generic types
        "DataFlowIssue", // Profile is not null on add action
        "unused" // Actually used, just via reflection
})
public class Loader_1_21 implements Loader {

    @Override
    @NotNull
    public String getLevelName(@NotNull Level level) {
        String path = level.dimension().location().getPath();
        return ((ServerLevelData)level.getLevelData()).getLevelName() + switch (path) {
            case "overworld" -> ""; // No suffix for overworld
            case "the_nether" -> "_nether";
            default -> "_" + path; // End + default behavior for other dimensions created by mods
        };
    }

    @Override
    @NotNull
    public TabList.Skin propertyToSkin(@NotNull Property property) {
        return new TabList.Skin(property.value(), property.signature());
    }

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return Component.literal(text);
    }

    @Override
    @NotNull
    public Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        TextColor color = null;
        if (modifier.getColor() != null) {
            if (modern) {
                color = TextColor.fromRgb(modifier.getColor().getRgb());
            } else {
                color = TextColor.fromRgb(modifier.getColor().getLegacyColor().getRgb());
            }
        }

        return new Style(
                color,
                modifier.isBold(),
                modifier.isItalic(),
                modifier.isUnderlined(),
                modifier.isStrikethrough(),
                modifier.isObfuscated(),
                null,
                null,
                null,
                modifier.getFont() == null ? null : ResourceLocation.tryParse(modifier.getFont())
        );
    }

    @Override
    public void addSibling(@NotNull Component parent, @NotNull Component child) {
        parent.getSiblings().add(child);
    }

    @Override
    @NotNull
    public Packet<?> registerTeam(@NotNull PlayerTeam team) {
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
    }

    @Override
    @NotNull
    public Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        return ClientboundSetPlayerTeamPacket.createRemovePacket(team);
    }

    @Override
    @NotNull
    public Packet<?> updateTeam(@NotNull PlayerTeam team) {
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
    }

    @Override
    public boolean isSneaking(@NotNull ServerPlayer player) {
        return player.isCrouching();
    }

    @Override
    public void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        player.sendSystemMessage(message);
    }

    @Override
    public void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        source.sendSystemMessage(message);
    }

    @Override
    @NotNull
    public Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        return new ClientboundTabListPacket(header, footer);
    }

    @Override
    public void checkTeamPacket(@NotNull Packet<?> packet, @NotNull FabricScoreboard scoreboard) {
        if (packet instanceof ClientboundSetPlayerTeamPacket team) {
            if (team.method == Scoreboard.TeamAction.UPDATE) return;
            team.players = scoreboard.onTeamPacket(team.method, team.getName(), team.players);
        }
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> spawnEntity(@NotNull Level level, int id, @NotNull UUID uuid, @NotNull Object type, @NotNull Location location) {
        return new ClientboundAddEntityPacket(id, uuid, location.getX(), location.getY(), location.getZ(),
                0, 0, (EntityType<?>) type, 0, Vec3.ZERO, 0);
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> newEntityMetadata(int entityId, @NotNull EntityData data) {
        return new ClientboundSetEntityDataPacket(entityId, (List<SynchedEntityData.DataValue<?>>) data.build());
    }

    @Override
    @NotNull
    public EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible, int markerPosition) {
        Optional<Component> name = Optional.of(TabComponent.optimized(displayName).convert(viewer.getVersion()));
        return () -> Arrays.asList(
                new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, flags),
                new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT, name),
                new SynchedEntityData.DataValue<>(3, EntityDataSerializers.BOOLEAN, nameVisible),
                new SynchedEntityData.DataValue<>(markerPosition, EntityDataSerializers.BYTE, EntityData.MARKER_FLAG)
        );
    }

    @Override
    public boolean isPlayerInfo(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundPlayerInfoUpdatePacket;
    }

    @Override
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet0) {
        ClientboundPlayerInfoUpdatePacket packet = (ClientboundPlayerInfoUpdatePacket) packet0;
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = packet.actions();
        List<ClientboundPlayerInfoUpdatePacket.Entry> updatedList = new ArrayList<>();
        for (ClientboundPlayerInfoUpdatePacket.Entry nmsData : packet.entries()) {
            GameProfile profile = nmsData.profile();
            Component displayName = nmsData.displayName();
            int latency = nmsData.latency();
            if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
                Component expectedDisplayName = ((FabricTabPlayer)receiver).getTabList().getExpectedDisplayName(nmsData.profileId());
                if (expectedDisplayName != null) displayName = expectedDisplayName;
            }
            if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)) {
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(receiver, nmsData.profileId(), latency);
            }
            if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, nmsData.profileId(), profile.getName());
            }
            updatedList.add(new ClientboundPlayerInfoUpdatePacket.Entry(nmsData.profileId(), profile, nmsData.listed(), latency, nmsData.gameMode(), displayName, nmsData.chatSession()));
        }
        packet.entries = updatedList;
    }

    @Override
    @NotNull
    public Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder entry) {
        if (action == TabList.Action.REMOVE_PLAYER) {
            return new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry.getId()));
        }
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(Register1_19_3.actionMap.get(action), Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoUpdatePacket.Entry(
                entry.getId(),
                action == TabList.Action.ADD_PLAYER ? entry.createProfile() : null,
                entry.isListed(),
                entry.getLatency(),
                GameType.byId(entry.getGameMode()),
                entry.getDisplayName(),
                null
        ));
        return packet;
    }

    @Override
    public boolean isBundlePacket(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundBundlePacket;
    }

    @Override
    @NotNull
    public Iterable<Object> getBundledPackets(@NotNull Packet<?> bundlePacket) {
        return (Iterable<Object>) (Object) ((ClientboundBundlePacket)bundlePacket).subPackets();
    }

    @Override
    @SneakyThrows
    public void sendPackets(@NotNull ServerPlayer player, @NotNull Iterable<Packet<ClientGamePacketListener>> packets) {
        // Reflection because
        // 1.20.4- uses Iterable<Packet<ClientGamePacketListener>
        // 1.20.5+ uses Iterable<Packet<? super ClientGamePacketListener>
        // this is the only way to merge them
        player.connection.send(ClientboundBundlePacket.class.getConstructor(Iterable.class).newInstance(packets));
    }

    @Override
    @NotNull
    public Level getLevel(@NotNull ServerPlayer player) {
        return player.level();
    }

    @Override
    public boolean isSpawnPlayerPacket(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundAddEntityPacket;
    }

    @Override
    public int getSpawnedPlayerId(@NotNull Packet<?> packet) {
        return ((ClientboundAddEntityPacket)packet).getId();
    }

    @Override
    public int getPing(@NotNull ServerPlayer player) {
        return player.connection.latency();
    }

    @Override
    public int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        return packet.getSlot().ordinal();
    }

    @Override
    @NotNull
    public Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        return new ClientboundSetDisplayObjectivePacket(DisplaySlot.values()[slot], objective);
    }

    @Override
    @NotNull
    public Channel getChannel(@NotNull ServerPlayer player) {
        return player.connection.connection.channel;
    }

    @Override
    public float getMSPT(@NotNull MinecraftServer server) {
        return (float) server.getAverageTickTimeNanos() / 1000000;
    }

    @Override
    @NotNull
    public Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        return new ClientboundResetScorePacket(holder, objective);
    }

    @Override
    public int[] getDestroyedEntities(@NotNull Packet<?> destroyPacket) {
        return ((ClientboundRemoveEntitiesPacket) destroyPacket).getEntityIds().toIntArray();
    }

    @Override
    @NotNull
    public Objective newObjective(@NotNull String name, @NotNull Component displayName,
                                  @NotNull RenderType renderType, @Nullable Component numberFormat) {
        return Register1_20_3.newObjective(name, displayName, renderType, numberFormat);
    }

    @Override
    @NotNull
    public Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable Component numberFormat) {
        return Register1_20_3.setScore(objective, holder, score, displayName, numberFormat);
    }

    @Override
    public void setStyle(@NotNull Component component, @NotNull Style style) {
        ((MutableComponent)component).setStyle(style);
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        MinecraftServer.LOGGER.info("[TAB] " + message.toRawText());
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        MinecraftServer.LOGGER.warn("[TAB] " + message.toRawText());
    }

    /**
     * Why is this needed? Because otherwise it throws error about a class
     * not existing despite the code never running.
     * Why? Nobody knows.
     */
    private static class Register1_20_3 {

        @NotNull
        public static Objective newObjective(@NotNull String name, @NotNull Component displayName,
                                      @NotNull RenderType renderType, @Nullable Component numberFormat) {
            return new Objective(dummyScoreboard, name, ObjectiveCriteria.DUMMY, displayName, renderType, false,
                    numberFormat == null ? null : new FixedFormat(numberFormat));
        }

        @NotNull
        @SneakyThrows
        public static Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable Component numberFormat) {
            try {
                // 1.20.5+
                return new ClientboundSetScorePacket(holder, objective, score, Optional.ofNullable(displayName),
                        Optional.ofNullable(numberFormat == null ? null : new FixedFormat(numberFormat)));
            } catch (Throwable t) {
                // 1.20.3 / 1.20.4
                return ClientboundSetScorePacket.class.getConstructor(String.class, String.class, int.class, Component.class, NumberFormat.class)
                        .newInstance(holder, objective, score, displayName, numberFormat == null ? null : new FixedFormat(numberFormat));
            }
        }
    }

    private static class Register1_19_3 {

        static final Map<TabList.Action, EnumSet<ClientboundPlayerInfoUpdatePacket.Action>> actionMap = createActionMap();

        public static EnumSet<ClientboundPlayerInfoUpdatePacket.Action> convertAction(TabList.Action action) {
            return EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.valueOf(action.name()));
        }

        private static Map<TabList.Action, EnumSet<ClientboundPlayerInfoUpdatePacket.Action>> createActionMap() {
            Map<TabList.Action, EnumSet<ClientboundPlayerInfoUpdatePacket.Action>> actions = new EnumMap<>(TabList.Action.class);
            actions.put(TabList.Action.ADD_PLAYER, EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class));
            actions.put(TabList.Action.UPDATE_GAME_MODE, EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE));
            actions.put(TabList.Action.UPDATE_DISPLAY_NAME, EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME));
            actions.put(TabList.Action.UPDATE_LATENCY, EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY));
            actions.put(TabList.Action.UPDATE_LISTED, EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED));
            return actions;
        }
    }
}
