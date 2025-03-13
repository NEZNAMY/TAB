package me.neznamy.tab.shared.features.nametags;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

/**
 * Proxy message to update team data of a player.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class NameTagUpdateProxyPlayer extends ProxyMessage {

    @NotNull private final NameTag feature;
    private UUID playerId;
    private String teamName;
    private String prefix;
    private String suffix;
    private Scoreboard.NameVisibility nameVisibility;

    @NotNull
    public ThreadExecutor getCustomThread() {
        return feature.getCustomThread();
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeUTF(teamName);
        out.writeUTF(prefix);
        out.writeUTF(suffix);
        out.writeUTF(nameVisibility.toString());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        teamName = in.readUTF();
        prefix = in.readUTF();
        suffix = in.readUTF();
        nameVisibility = Scoreboard.NameVisibility.getByName(in.readUTF());
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            TAB.getInstance().getErrorManager().printError("Unable to process nametag update of proxy player " + playerId + ", because no such player exists", null);
            return;
        }
        if (target.getTeamName() == null) {
            TAB.getInstance().debug("Processing nametag join of proxy player " + target.getName());
        }
        // Nametag is already being processed by connected player
        if (TAB.getInstance().isPlayerConnected(target.getUniqueId())) {
            TAB.getInstance().debug("The player " + target.getName() + " is already connected");
            return;
        }
        String oldTeamName = target.getTeamName();
        String newTeamName = checkTeamName(target, teamName.substring(0, teamName.length()-1));
        target.setTeamName(newTeamName);
        target.setTagPrefix(prefix);
        target.setTagSuffix(suffix);
        target.setNameVisibility(nameVisibility);
        TabComponent prefixComponent = feature.getCache().get(prefix);
        if (!newTeamName.equals(oldTeamName)) {
            for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
                if (oldTeamName != null) viewer.getScoreboard().unregisterTeam(oldTeamName);
                viewer.getScoreboard().registerTeam(
                        newTeamName,
                        prefixComponent,
                        feature.getCache().get(suffix),
                        nameVisibility,
                        Scoreboard.CollisionRule.ALWAYS,
                        Collections.singletonList(target.getNickname()),
                        2,
                        prefixComponent.getLastColor()
                );
            }
        } else {
            for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
                viewer.getScoreboard().updateTeam(
                        oldTeamName,
                        prefixComponent,
                        feature.getCache().get(suffix),
                        nameVisibility,
                        Scoreboard.CollisionRule.ALWAYS,
                        2,
                        prefixComponent.getLastColor()
                );
            }
        }
    }

    @NotNull
    private String checkTeamName(@NotNull ProxyPlayer player, @NotNull String currentName15) {
        char id = 'A';
        while (true) {
            String potentialTeamName = currentName15 + id;
            boolean nameTaken = false;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (potentialTeamName.equals(all.sortingData.shortTeamName)) {
                    nameTaken = true;
                    break;
                }
            }
            if (!nameTaken && feature.getProxy() != null) {
                for (ProxyPlayer all : feature.getProxy().getProxyPlayers().values()) {
                    if (all == player) continue;
                    if (potentialTeamName.equals(all.getTeamName())) {
                        nameTaken = true;
                        break;
                    }
                }
            }
            if (!nameTaken) {
                return potentialTeamName;
            }
            id++;
        }
    }
}