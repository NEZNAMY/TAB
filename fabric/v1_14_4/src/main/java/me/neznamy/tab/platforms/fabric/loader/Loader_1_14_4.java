package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.fabric.FabricScoreboard;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Implementation containing methods in the state of the oldest supported
 * Minecraft version by the mod - 1.14.4.
 */
@SuppressWarnings({
        "unchecked", // Java generic types
        "unused" // Actually used, just via reflection
})
@RequiredArgsConstructor
public class Loader_1_14_4 implements Loader {

    private final ProtocolVersion serverVersion;

    @Override
    @NotNull
    public String getLevelName(@NotNull Level level) {
        return level.getLevelData().getLevelName() + level.dimension.getType().getFileSuffix();
    }

    @Override
    @NotNull
    public TabList.Skin propertyToSkin(@NotNull Property property) {
        return new TabList.Skin(property.getValue(), property.getSignature());
    }

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return new TextComponent(text);
    }

    @Override
    @NotNull
    public Style convertModifier(@NotNull ChatModifier modifier, boolean modern) {
        Style style = new Style();
        if (modifier.getColor() != null) {
            style.setColor(ChatFormatting.valueOf(modifier.getColor().getLegacyColor().name()));
        }
        if (modifier.isBold()) style.setBold(true);
        if (modifier.isItalic()) style.setItalic(true);
        if (modifier.isStrikethrough()) style.setStrikethrough(true);
        if (modifier.isUnderlined()) style.setUnderlined(true);
        if (modifier.isObfuscated()) style.setObfuscated(true);
        return style;
    }

    @Override
    public void addSibling(@NotNull Component parent, @NotNull Component child) {
        parent.append(child);
    }

    @Override
    @NotNull
    public Packet<?> registerTeam(@NotNull PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, 0);
    }

    @Override
    @NotNull
    public Packet<?> unregisterTeam(@NotNull PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, 1);
    }

    @Override
    @NotNull
    public Packet<?> updateTeam(@NotNull PlayerTeam team) {
        return new ClientboundSetPlayerTeamPacket(team, 2);
    }

    @Override
    public void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull CommandSourceStack source, @NotNull Component message) {
        source.sendSuccess(message, false);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Packet<?> newHeaderFooter(@NotNull Component header, @NotNull Component footer) {
        ClientboundTabListPacket packet = ClientboundTabListPacket.class.getConstructor().newInstance();
        List<Field> fields = ReflectionUtils.getFields(ClientboundTabListPacket.class, Component.class);
        fields.get(0).set(packet, header);
        fields.get(1).set(packet, footer);
        return packet;
    }

    @Override
    @SneakyThrows
    public void checkTeamPacket(@NotNull Packet<?> packet, @NotNull FabricScoreboard scoreboard) {
        if (packet instanceof ClientboundSetPlayerTeamPacket) {
            int action = ReflectionUtils.getInstanceFields(packet.getClass(), int.class).get(0).getInt(packet);
            if (action == Scoreboard.TeamAction.UPDATE) return;
            Field playersField = ReflectionUtils.getFields(packet.getClass(), Collection.class).get(0);
            Collection<String> players = (Collection<String>) playersField.get(packet);
            String teamName = String.valueOf(ReflectionUtils.getFields(packet.getClass(), String.class).get(0).get(packet));
            playersField.set(packet, scoreboard.onTeamPacket(action, teamName, players));
        }
    }

    @Override
    public boolean isPlayerInfo(@NotNull Packet<?> packet) {
        return packet instanceof ClientboundPlayerInfoPacket;
    }

    @Override
    @SneakyThrows
    public void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet) {
        ClientboundPlayerInfoPacket.Action action = (ClientboundPlayerInfoPacket.Action) ReflectionUtils.getFields(packet.getClass(), ClientboundPlayerInfoPacket.Action.class).get(0).get(packet);
        List<PlayerUpdate> players = (List<PlayerUpdate>) ReflectionUtils.getFields(packet.getClass(), List.class).get(0).get(packet);
        for (PlayerUpdate nmsData : players) {
            GameProfile profile = nmsData.getProfile();
            Field displayNameField = ReflectionUtils.getFields(PlayerUpdate.class, Component.class).get(0);
            Field latencyField = ReflectionUtils.getFields(PlayerUpdate.class, int.class).get(0);
            if (action.name().equals(TabList.Action.UPDATE_DISPLAY_NAME.name()) || action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                Object expectedName = ((FabricTabList)receiver.getTabList()).getExpectedDisplayNames().get(profile.getId());
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
    @SneakyThrows
    public Packet<?> buildTabListPacket(TabList.@NotNull Action action, @NotNull FabricTabList.Builder entry) {
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()));
        ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet, Collections.singletonList(
                packet.new PlayerUpdate(entry.createProfile(), entry.getLatency(), GameType.byId(entry.getGameMode()), entry.getDisplayName())));
        return packet;
    }

    @Override
    @NotNull
    public Level getLevel(@NotNull ServerPlayer player) {
        return player.level;
    }

    @Override
    public int getPing(@NotNull ServerPlayer player) {
        return player.latency;
    }

    @Override
    @SneakyThrows
    public int getDisplaySlot(@NotNull ClientboundSetDisplayObjectivePacket packet) {
        return ReflectionUtils.getFields(packet.getClass(), int.class).get(0).getInt(packet);
    }

    @Override
    @NotNull
    public Packet<?> setDisplaySlot(int slot, @NotNull Objective objective) {
        return new ClientboundSetDisplayObjectivePacket(slot, objective);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Channel getChannel(@NotNull ServerPlayer player) {
        Connection c = (Connection) ReflectionUtils.getFields(ServerGamePacketListenerImpl.class, Connection.class).get(0).get(player.connection);
        return (Channel) ReflectionUtils.getFields(Connection.class, Channel.class).get(0).get(c);
    }

    @Override
    public float getMSPT(@NotNull MinecraftServer server) {
        return server.getAverageTickTime();
    }

    @Override
    @NotNull
    public Packet<?> removeScore(@NotNull String objective, @NotNull String holder) {
        return new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, holder, 0);
    }

    @Override
    @NotNull
    public Objective newObjective(@NotNull String name, @NotNull Component displayName,
                                  @NotNull RenderType renderType, @Nullable TabComponent numberFormat) {
        return new Objective(dummyScoreboard, name, ObjectiveCriteria.DUMMY, displayName, renderType);
    }

    @Override
    @NotNull
    public Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable TabComponent numberFormat) {
        return new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, holder, score);
    }

    @Override
    public void setStyle(@NotNull Component component, @NotNull Style style) {
        component.setStyle(style);
    }

    @Override
    @SneakyThrows
    public void logInfo(@NotNull TabComponent message) {
        Object logger = ReflectionUtils.getFields(MinecraftServer.class, Class.forName("org.apache.logging.log4j.Logger")).get(0).get(null);
        logger.getClass().getMethod("info", String.class).invoke(logger, "[TAB] " + message.toRawText());
    }

    @Override
    @SneakyThrows
    public void logWarn(@NotNull TabComponent message) {
        Object logger = ReflectionUtils.getFields(MinecraftServer.class, Class.forName("org.apache.logging.log4j.Logger")).get(0).get(null);
        logger.getClass().getMethod("warn", String.class).invoke(logger, "[TAB] " + message.toRawText());
    }

    @NotNull
    @Override
    public CommandSourceStack createCommandSourceStack(@NotNull ServerPlayer player) {
        return player.createCommandSourceStack();
    }
}
