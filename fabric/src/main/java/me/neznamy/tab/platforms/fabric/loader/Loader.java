package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.UUID;

public interface Loader {

    Scoreboard dummyScoreboard = new Scoreboard();

    @NotNull
    String getLevelName(@NotNull Level level);

    @NotNull
    TabList.Skin propertyToSkin(@NotNull Property property);

    @NotNull
    Component newTextComponent(@NotNull String text);

    @NotNull
    Style convertModifier(@NotNull ChatModifier modifier, @NotNull ProtocolVersion version);

    void addSibling(@NotNull Component parent, @NotNull Component child);

    @NotNull
    Packet<?> registerTeam(@NotNull PlayerTeam team);

    @NotNull
    Packet<?> unregisterTeam(@NotNull PlayerTeam team);

    @NotNull
    Packet<?> updateTeam(@NotNull PlayerTeam team);

    boolean isSneaking(@NotNull ServerPlayer player);

    void sendMessage(@NotNull ServerPlayer player, @NotNull Component message);

    void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message);

    @NotNull
    Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer);

    boolean isTeamPacket(@NotNull Packet<?> packet);

    @NotNull
    Packet<ClientGamePacketListener> spawnEntity(@NotNull Level level, int id, @NotNull UUID uuid, @NotNull Object type, @NotNull Location location);

    @NotNull
    Packet<ClientGamePacketListener> newEntityMetadata(int entityId, @NotNull EntityData data);

    @NotNull
    EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible, int markerPosition);

    boolean isPlayerInfo(@NotNull Packet<?> packet);

    void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet);

    @NotNull
    Packet<?> buildTabListPacket(@NotNull TabList.Action action, @NotNull FabricTabList.Builder builder);

    boolean isBundlePacket(@NotNull Packet<?> packet);

    @NotNull
    Iterable<Object> getBundledPackets(@NotNull Packet<?> bundlePacket);

    void sendPackets(@NotNull ServerPlayer player, @NotNull Iterable<Packet<ClientGamePacketListener>> packets);

    @NotNull
    Level getLevel(@NotNull ServerPlayer player);

    boolean isSpawnPlayerPacket(@NotNull Packet<?> packet);

    int getPing(@NotNull ServerPlayer player);

    int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet);

    @NotNull
    Packet<?> setDisplaySlot(int slot, @NotNull Objective objective);

    @NotNull
    Channel getChannel(@NotNull ServerPlayer player);

    float getMSPT(@NotNull MinecraftServer server);

    @NotNull
    Packet<?> removeScore(@NotNull String objective, @NotNull String holder);

    int[] getDestroyedEntities(Packet<?> destroyPacket);

    @NotNull
    Objective newObjective(@NotNull String name, @NotNull Component displayName, @NotNull RenderType renderType, @Nullable Component numberFormat);

    @NotNull
    Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable Component numberFormat);

    void setStyle(@NotNull Component component, @NotNull Style style);

    void logInfo(@NotNull TabComponent message);

    void logWarn(@NotNull TabComponent message);
}
