package me.neznamy.tab.platforms.krypton;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import org.kryptonmc.api.command.Sender;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.api.event.Listener;
import org.kryptonmc.api.event.command.CommandExecuteEvent;
import org.kryptonmc.api.event.player.PlayerJoinEvent;
import org.kryptonmc.api.event.player.PlayerQuitEvent;
import org.kryptonmc.api.scoreboard.Scoreboard;

@RequiredArgsConstructor
public class KryptonEventListener {

    private final KryptonTAB plugin;

    @Listener
    public void onJoin(PlayerJoinEvent event) {
        if (TAB.getInstance().isPluginDisabled()) return;

        // Create a new, blank scoreboard for each player to avoid conflicts
        event.getPlayer().showScoreboard(Scoreboard.create());

        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onJoin(
                new KryptonTabPlayer(event.getPlayer(), plugin.getProtocolVersion(event.getPlayer()))));
    }

    @Listener
    public void onQuit(PlayerQuitEvent event) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onQuit(
                TAB.getInstance().getPlayer(event.getPlayer().getUuid())));
    }

    @Listener
    public void onCommand(CommandExecuteEvent event) {
        if (TAB.getInstance().isPluginDisabled()) return;

        Sender player = event.getSender();
        if (!(player instanceof Player)) return;

        if (TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((Player) player).getUuid()), event.getCommand())) {
            event.deny();
        }
    }
}
