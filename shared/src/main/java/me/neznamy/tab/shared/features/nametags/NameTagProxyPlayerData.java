package me.neznamy.tab.shared.features.nametags;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

/**
 * Proxy message to update team data of a player.
 */
@RequiredArgsConstructor
@ToString(exclude = "feature")
@Getter
public class NameTagProxyPlayerData extends ProxyMessage {

    @NotNull private final NameTag feature;
    private final long id;
    @NotNull private final UUID playerId;
    @NotNull private final String teamName;
    @NotNull private final String prefix;
    @NotNull private final String suffix;
    @NotNull private final Scoreboard.NameVisibility nameVisibility;
    @Nullable private String resolvedTeamName;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     * @param   feature
     *          Feature instance to use for processing
     */
    public NameTagProxyPlayerData(@NotNull ByteArrayDataInput in, @NotNull NameTag feature) {
        this.feature = feature;
        id = in.readLong();
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
        out.writeLong(id);
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
            unknownPlayer(playerId.toString(), "nametag update update");
            QueuedData data = proxySupport.getQueuedData().computeIfAbsent(playerId, k -> new QueuedData());
            if (data.getNametag() == null || data.getNametag().id < id) {
                resolvedTeamName = checkTeamName(null, teamName.substring(0, teamName.length()-1));
                data.setNametag(this);
            }
            return;
        }
        if (target.getNametag() != null && target.getNametag().id > id) {
            TAB.getInstance().debug("Dropping nametag update action for player " + target.getName() + " due to newer action already being present");
            return;
        }
        NameTagProxyPlayerData oldData = target.getNametag();
        resolvedTeamName = checkTeamName(target, teamName.substring(0, teamName.length()-1));
        target.setNametag(this);

        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            TabComponent prefix = feature.getPrefixCache().get(this.prefix);
            TabComponent suffix = feature.getSuffixCache().get(this.suffix);
            for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
                if (oldData != null && resolvedTeamName.equals(oldData.resolvedTeamName)) {
                    viewer.getScoreboard().updateTeam(
                            oldData.teamName,
                            prefix,
                            suffix,
                            nameVisibility,
                            Scoreboard.CollisionRule.ALWAYS,
                            feature.getTeamOptions(),
                            prefix.getLastStyle().toEnumChatFormat()
                    );
                } else {
                    if (oldData != null) {
                        viewer.getScoreboard().unregisterTeam(oldData.resolvedTeamName);
                    }
                    viewer.getScoreboard().registerTeam(
                            resolvedTeamName,
                            prefix,
                            suffix,
                            nameVisibility,
                            Scoreboard.CollisionRule.ALWAYS,
                            Collections.singletonList(target.getNickname()),
                            feature.getTeamOptions(),
                            prefix.getLastStyle().toEnumChatFormat()
                    );
                }
            }
        }
    }

    @NotNull
    private String checkTeamName(@Nullable ProxyPlayer player, @NotNull String currentName15) {
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
                    if (all.getNametag() != null && potentialTeamName.equals(all.getNametag().teamName)) {
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