package me.neznamy.tab.shared.features.belowname;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Proxy message to update belowname data of a player.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class BelowNameUpdateProxyPlayer extends ProxyMessage {

    @NotNull
    private final BelowName feature;

    private UUID playerId;
    private int value;
    private String fancyValue;

    @NotNull
    public ThreadExecutor getCustomThread() {
        return feature.getCustomThread();
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeInt(value);
        out.writeUTF(fancyValue);
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        value = in.readInt();
        fancyValue = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            TAB.getInstance().getErrorManager().printError("Unable to process Belowname objective update of proxy player " + playerId + ", because no such player exists", null);
            return;
        }
        if (target.getBelowNameFancy() == null) {
            TAB.getInstance().debug("Processing belowname objective join of proxy player " + target.getName());
        }
        target.setBelowNameNumber(value);
        target.setBelowNameFancy(feature.getCache().get(fancyValue));
        for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
            if (viewer.belowNameData.disabled.get()) continue;
            viewer.getScoreboard().setScore(
                    BelowName.OBJECTIVE_NAME,
                    target.getNickname(),
                    target.getBelowNameNumber(),
                    null, // Unused by this objective slot
                    target.getBelowNameFancy()
            );
        }
    }
}
