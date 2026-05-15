package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VelocityEventListener implements EventListener<Player> {

    private final TAB tab = TAB.getInstance();
    private final Map<Player, UUID> players = new ConcurrentHashMap<>();

    @Subscribe
    public void onQuit(@NotNull DisconnectEvent e) {
        if (tab.isPluginDisabled()) return;

        UUID id = players.remove(e.getPlayer());
        if (id != null) quit(id);
    }

    @Subscribe
    public void preConnect(@NotNull ServerPreConnectEvent e) {
        if (tab.isPluginDisabled()) return;
        if (!e.getResult().isAllowed()) return;

        TabPlayer p = tab.getPlayer(e.getPlayer().getUniqueId());
        if (p == null) return;

        if (p.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
            SafeBossBar<?> bossBar = (SafeBossBar<?>) p.getBossBar();
            bossBar.freeze();
        }
    }

    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent e) {
        if (tab.isPluginDisabled()) return;

        tab.getCPUManager().runTask(() -> handlePostConnect(e));
    }

    private void handlePostConnect(ServerPostConnectEvent e) {
        Player proxyPlayer = e.getPlayer();
        UUID uuid = proxyPlayer.getUniqueId();

        TabPlayer player = tab.getPlayer(uuid);

        if (player == null) {
            players.put(proxyPlayer, uuid);
            tab.getFeatureManager().onJoin(createPlayer(proxyPlayer));
            return;
        }

        if (!(player.getScoreboard() instanceof VelocityScoreboard)) {
            player.getScoreboard().resend();
        }

        String serverName = proxyPlayer.getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("null");

        tab.getFeatureManager().onServerChange(
                uuid,
                Server.byName(serverName)
        );

        tab.getFeatureManager().onTabListClear(player);

        if (player.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
            ((SafeBossBar<?>) player.getBossBar()).unfreezeAndSynchronize();
        }
    }

    @Subscribe
    public void onPluginMessageEvent(@NotNull PluginMessageEvent e) {
        if (!TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.equals(e.getIdentifier().getId())) return;

        e.setResult(PluginMessageEvent.ForwardResult.handled());

        if (tab.isPluginDisabled()) return;

        if (e.getTarget() instanceof Player player) {
            pluginMessage(player.getUniqueId(), e.getData());
        }
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull Player player) {
        return new VelocityTabPlayer(
                (VelocityPlatform) tab.getPlatform(),
                player
        );
    }
}
