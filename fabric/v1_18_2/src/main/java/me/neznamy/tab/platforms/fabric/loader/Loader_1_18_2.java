package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation containing some methods that have changed multiple times
 * throughout the versions and need a separate module. This module implements
 * a few methods in the state of Minecraft 1.17 - 1.18.2.
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
                TabComponent expectedName = ((TrackedTabList<?>)receiver.getTabList()).getExpectedDisplayNames().get(profile.getId());
                if (expectedName != null) displayNameField.set(nmsData, expectedName.convert());
            }
            if (action.name().equals(TabList.Action.UPDATE_LATENCY.name()) || action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
            }
            if (action.name().equals(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
            }
        }
    }
}
