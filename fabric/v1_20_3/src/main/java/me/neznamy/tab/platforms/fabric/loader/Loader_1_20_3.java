package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.fabric.FabricTabList;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Implementation containing some methods that have changed multiple times
 * throughout the versions and need a separate module. This module implements
 * a few methods in the state of Minecraft 1.20.3 - 1.20.4.
 */
@SuppressWarnings("unused") // Actually used, just via reflection
@RequiredArgsConstructor
public class Loader_1_20_3 implements Loader {

    private final ProtocolVersion serverVersion;

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
                Component expectedDisplayName = ((FabricTabList)receiver.getTabList()).getExpectedDisplayNames().get(nmsData.profileId());
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
    public void sendMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        player.sendSystemMessage(message);
    }

    private static class Register1_19_3 {

        static final Map<TabList.Action, EnumSet<ClientboundPlayerInfoUpdatePacket.Action>> actionMap = createActionMap();

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

    @Override
    @NotNull
    public Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable TabComponent numberFormat) {
        return Register1_20_3.setScore(objective, holder, score, displayName, numberFormat);
    }

    private static class Register1_20_3 {

        @NotNull
        public static Packet<?> setScore(@NotNull String objective, @NotNull String holder, int score, @Nullable Component displayName, @Nullable TabComponent numberFormat) {
            return new ClientboundSetScorePacket(holder, objective, score, displayName, Loader_Latest.Register1_20_3.toFixedFormat(numberFormat));
        }
    }
}
