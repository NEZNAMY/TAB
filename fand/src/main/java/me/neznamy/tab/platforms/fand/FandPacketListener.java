package me.neznamy.tab.platforms.fand;

import io.fand.api.packet.PacketController;
import io.fand.api.packet.PacketPriority;
import io.fand.api.packet.PacketType;
import io.fand.api.packet.view.ClientboundPlayerInfoRemovePacketView;
import io.fand.api.packet.view.ClientboundPlayerInfoUpdatePacketView;
import io.fand.api.packet.view.ClientboundSetDisplayObjectivePacketView;
import io.fand.api.packet.view.ClientboundSetObjectivePacketView;
import io.fand.api.packet.view.ClientboundSetPlayerTeamPacketView;
import io.fand.api.packet.view.ClientboundTabListPacketView;
import io.fand.api.plugin.PluginContext;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Applies TAB's player-info anti-override rules through Fand's packet API. */
final class FandPacketListener {

    private final PluginContext context;

    FandPacketListener(@NotNull PluginContext context) {
        this.context = context;
    }

    void register() {
        context.packets().intercept(
                PacketType.PLAY_CLIENTBOUND_PLAYER_INFO_UPDATE,
                ClientboundPlayerInfoUpdatePacketView.class,
                PacketPriority.HIGHEST,
                this::rewritePlayerInfo);
        context.packets().intercept(
                PacketType.PLAY_CLIENTBOUND_PLAYER_INFO_REMOVE,
                ClientboundPlayerInfoRemovePacketView.class,
                PacketPriority.HIGHEST,
                this::observePlayerInfoRemove);
        context.packets().intercept(
                PacketType.PLAY_CLIENTBOUND_TAB_LIST,
                ClientboundTabListPacketView.class,
                PacketPriority.HIGHEST,
                this::rewriteHeaderFooter);
        context.packets().intercept(
                PacketType.PLAY_CLIENTBOUND_SET_DISPLAY_OBJECTIVE,
                ClientboundSetDisplayObjectivePacketView.class,
                PacketPriority.HIGHEST,
                this::observeDisplayObjective);
        context.packets().intercept(
                PacketType.PLAY_CLIENTBOUND_SET_OBJECTIVE,
                ClientboundSetObjectivePacketView.class,
                PacketPriority.HIGHEST,
                this::observeObjective);
        context.packets().intercept(
                PacketType.PLAY_CLIENTBOUND_SET_PLAYER_TEAM,
                ClientboundSetPlayerTeamPacketView.class,
                PacketPriority.HIGHEST,
                this::rewriteTeam);
    }

    private void rewritePlayerInfo(PacketController<ClientboundPlayerInfoUpdatePacketView> packet) {
        FandTabList tabList = tabList(packet);
        if (tabList == null) {
            return;
        }
        var replacement = tabList.rewritePlayerInfo(packet.view());
        if (replacement != packet.view()) {
            packet.replace(replacement);
        }
    }

    private void observePlayerInfoRemove(PacketController<ClientboundPlayerInfoRemovePacketView> packet) {
        FandTabList tabList = tabList(packet);
        if (tabList != null) {
            tabList.observePlayerInfoRemove(packet.view());
        }
    }

    private void rewriteHeaderFooter(PacketController<ClientboundTabListPacketView> packet) {
        FandTabList tabList = tabList(packet);
        if (tabList == null) {
            return;
        }
        var replacement = tabList.rewriteHeaderFooter(packet.view());
        if (replacement != packet.view()) {
            packet.replace(replacement);
        }
    }

    private void observeDisplayObjective(PacketController<ClientboundSetDisplayObjectivePacketView> packet) {
        FandScoreboard scoreboard = scoreboard(packet);
        if (scoreboard != null) {
            scoreboard.observeDisplayObjective(packet.view());
        }
    }

    private void observeObjective(PacketController<ClientboundSetObjectivePacketView> packet) {
        FandScoreboard scoreboard = scoreboard(packet);
        if (scoreboard != null) {
            scoreboard.observeObjective(packet.view());
        }
    }

    private void rewriteTeam(PacketController<ClientboundSetPlayerTeamPacketView> packet) {
        FandScoreboard scoreboard = scoreboard(packet);
        if (scoreboard == null) {
            return;
        }
        var replacement = scoreboard.rewriteTeam(packet.view());
        if (replacement != packet.view()) {
            packet.replace(replacement);
        }
    }

    @Nullable
    private static FandTabList tabList(PacketController<?> packet) {
        FandTabPlayer player = player(packet);
        return player != null && player.getTabList() instanceof FandTabList tabList ? tabList : null;
    }

    @Nullable
    private static FandScoreboard scoreboard(PacketController<?> packet) {
        FandTabPlayer player = player(packet);
        return player != null && player.getScoreboard() instanceof FandScoreboard scoreboard ? scoreboard : null;
    }

    @Nullable
    private static FandTabPlayer player(PacketController<?> packet) {
        var viewer = packet.context().player().orElse(null);
        if (viewer == null || TAB.getInstance() == null || TAB.getInstance().isPluginDisabled()) {
            return null;
        }
        var tabPlayer = TAB.getInstance().getPlayer(viewer.uniqueId());
        return tabPlayer instanceof FandTabPlayer fandPlayer ? fandPlayer : null;
    }
}
