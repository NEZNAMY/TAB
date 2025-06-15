package me.neznamy.tab.shared.features.nametags;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.ToString;
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
@AllArgsConstructor
@ToString
public class NameTagUpdateProxyPlayer extends ProxyMessage {

    @NotNull private final NameTag feature;
    @NotNull private final UUID playerId;
    @NotNull private final String teamName;
    @NotNull private final String prefix;
    @NotNull private final String suffix;
    @NotNull private final Scoreboard.NameVisibility nameVisibility;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     * @param   feature
     *          Feature instance to use for processing
     */
    public NameTagUpdateProxyPlayer(@NotNull ByteArrayDataInput in, @NotNull NameTag feature) {
        this.feature = feature;
        playerId = readUUID(in);
        teamName = in.readUTF();
        prefix = in.readUTF();
        suffix = in.readUTF();
        nameVisibility = Scoreboard.NameVisibility.getByName(in.readUTF());
    }

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
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            TAB.getInstance().getErrorManager().proxyMessageUnknownPlayer(playerId.toString(), "nametag update update");
            return;
        }
        String oldTeamName = target.getTeamName();
        String newTeamName = checkTeamName(target, teamName.substring(0, teamName.length()-1));
        target.setTeamName(newTeamName);
        target.setTagPrefix(feature.getCache().get(prefix));
        target.setTagSuffix(feature.getCache().get(suffix));
        target.setNameVisibility(nameVisibility);

        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            if (!newTeamName.equals(oldTeamName)) {
                for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
                    if (oldTeamName != null) viewer.getScoreboard().unregisterTeam(oldTeamName);
                    viewer.getScoreboard().registerTeam(
                            newTeamName,
                            target.getTagPrefix(),
                            target.getTagSuffix(),
                            nameVisibility,
                            Scoreboard.CollisionRule.ALWAYS,
                            Collections.singletonList(target.getNickname()),
                            2,
                            target.getTagPrefix().getLastColor()
                    );
                }
            } else {
                for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
                    viewer.getScoreboard().updateTeam(
                            oldTeamName,
                            target.getTagPrefix(),
                            target.getTagSuffix(),
                            nameVisibility,
                            Scoreboard.CollisionRule.ALWAYS,
                            2,
                            target.getTagPrefix().getLastColor()
                    );
                }
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