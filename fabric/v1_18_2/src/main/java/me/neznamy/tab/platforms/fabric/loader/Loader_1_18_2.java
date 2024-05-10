package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.FabricScoreboard;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Method loader compiled using Minecraft containing method implementations from 1.17 - 1.18.2.
 */
@SuppressWarnings("unused") // Actually used, just via reflection
@RequiredArgsConstructor
public class Loader_1_18_2 implements Loader {

    private final ProtocolVersion serverVersion;

    @Override
    public void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        player.sendMessage(message, new UUID(0, 0));
    }

    @Override
    public void setStyle(@NotNull Component component, @NotNull Style style) {
        ((BaseComponent)component).setStyle(style);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder entry) {
        PlayerUpdate update;
        if (serverVersion.getMinorVersion() >= 19) {
            // 1.19 - 1.19.2
            update = (PlayerUpdate) PlayerUpdate.class.getConstructors()[0].newInstance(
                    entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName(), null);
        } else {
            update = new PlayerUpdate(entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName());
        }
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()));
        ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet, Collections.singletonList(update));
        return packet;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        ClientboundPlayerInfoPacket.Action action = (ClientboundPlayerInfoPacket.Action) ReflectionUtils.getFields(packet.getClass(), ClientboundPlayerInfoPacket.Action.class).get(0).get(packet);
        List<PlayerUpdate> players = (List<PlayerUpdate>) ReflectionUtils.getFields(packet.getClass(), List.class).get(0).get(packet);
        for (PlayerUpdate nmsData : players) {
            GameProfile profile = nmsData.getProfile();
            Field displayNameField = ReflectionUtils.getFields(PlayerUpdate.class, Component.class).get(0);
            Field latencyField = ReflectionUtils.getFields(PlayerUpdate.class, int.class).get(0);
            if (action.name().equals(TabList.Action.UPDATE_DISPLAY_NAME.name()) || action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                Object expectedName = ((FabricTabPlayer)receiver).getTabList().getExpectedDisplayName(profile.getId());
                if (expectedName != null) displayNameField.set(nmsData, expectedName);
            }
            if (action.name().equals(TabList.Action.UPDATE_LATENCY.name()) || action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
            }
            if (action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
            }
        }
    }

    @Override
    @NotNull
    public String getLevelName(@NotNull Level level) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @NotNull
    @Override
    public TabList.Skin propertyToSkin(@NotNull Property property) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public void addSibling(@NotNull Component parent, @NotNull Component child) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> registerTeam(@NotNull PlayerTeam team) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> updateTeam(@NotNull PlayerTeam team) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public boolean isSneaking(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public void checkTeamPacket(@NotNull Packet<?> packet, @NotNull FabricScoreboard scoreboard) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> spawnEntity(@NotNull Level level, int id, @NotNull UUID uuid, @NotNull Object type, @NotNull Location location) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> newEntityMetadata(int entityId, @NotNull EntityData data) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible, int markerPosition) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public boolean isPlayerInfo(@NotNull Packet<?> packet) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public boolean isBundlePacket(@NotNull Packet<?> packet) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Iterable<Object> getBundledPackets(@NotNull Packet<?> bundlePacket) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public void sendPackets(@NotNull ServerPlayer player, @NotNull Iterable<Packet<ClientGamePacketListener>> packets) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Level getLevel(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public boolean isSpawnPlayerPacket(@NotNull Packet<?> packet) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public int getSpawnedPlayerId(@NotNull Packet<?> packet) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public int getPing(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Channel getChannel(@NotNull ServerPlayer player) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public float getMSPT(@NotNull MinecraftServer server) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public int[] getDestroyedEntities(@NotNull Packet<?> destroyPacket) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Objective newObjective(@NotNull String name, @NotNull Component displayName, ObjectiveCriteria.@NotNull RenderType renderType, @Nullable Component numberFormat) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    @NotNull
    public Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable Component numberFormat) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        throw new UnsupportedOperationException("Not implemented in this submodule");
    }
}
