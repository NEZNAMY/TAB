package me.neznamy.tab.shared.features.nametags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.types.ProxyFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Class for handling proxy players in NameTag feature.
 * Separated to avoid the main class getting too massive.
 */
@RequiredArgsConstructor
public class NameTagProxyHandler implements ProxyFeature {

    @NotNull
    private final NameTag feature;

    public void sendProxyMessage(@NotNull TabPlayer player) {
        if (feature.getProxy() != null) {
            feature.getProxy().sendMessage(new NameTagProxyPlayerData(
                    feature,
                    feature.getProxy().getIdCounter().incrementAndGet(),
                    player.getUniqueId(),
                    player.teamData.teamName,
                    player.teamData.prefix.get(),
                    player.teamData.suffix.get(),
                    feature.getTeamVisibility(player, player) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER
            ));
        }
    }

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : feature.getOnlinePlayers().getPlayers()) {
            sendProxyMessage(all);
        }
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        if (player.getNametag() == null) {
            // One of the two options is being forcibly unregistered when real player joined
            return;
        }
        for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
            ((SafeScoreboard<?>)viewer.getScoreboard()).unregisterTeamSafe(player.getNametag().getResolvedTeamName());
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        if (player.getNametag() == null) return; // Player not loaded yet
        for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
            viewer.getScoreboard().registerTeam(
                    player.getNametag().getResolvedTeamName(),
                    feature.getPrefixCache().get(player.getNametag().getPrefix()),
                    feature.getSuffixCache().get(player.getNametag().getSuffix()),
                    player.getNametag().getNameVisibility(),
                    Scoreboard.CollisionRule.ALWAYS,
                    Collections.singletonList(player.getNickname()),
                    feature.getTeamOptions(),
                    feature.getLastColorCache().get(player.getNametag().getPrefix()).getLastStyle().toEnumChatFormat()
            );
        }
    }
}
