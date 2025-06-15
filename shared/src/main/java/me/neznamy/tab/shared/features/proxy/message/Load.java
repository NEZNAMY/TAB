package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.ToString;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message sent by another proxy to load multiple players.
 */
@ToString
public class Load extends ProxyMessage {

    @NotNull private final List<PlayerJoin> decodedPlayers;

    /**
     * Creates new instance from given players.
     *
     * @param   players
     *          Players to encode
     */
    public Load(@NotNull TabPlayer[] players) {
        decodedPlayers = Arrays.stream(players).map(PlayerJoin::new).collect(Collectors.toList());
    }

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     */
    public Load(@NotNull ByteArrayDataInput in) {
        decodedPlayers = new ArrayList<>();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            decodedPlayers.add(new PlayerJoin(in));
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        out.writeInt(decodedPlayers.size());
        for (PlayerJoin player : decodedPlayers) {
            player.write(out);
        }
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        for (PlayerJoin join : decodedPlayers) {
            join.process(proxySupport);
        }
    }
}
