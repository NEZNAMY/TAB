package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class SetScoreboardTeam implements OutgoingMessage {

    private String name;
    private int action;
    private String prefix;
    private String suffix;
    private int options;
    private String visibility;
    private String collision;
    private int color;
    private Collection<String> players;

    public SetScoreboardTeam(String name) {
        this.name = name;
        action = Scoreboard.TeamAction.REMOVE;
    }

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PacketPlayOutScoreboardTeam");
        out.writeUTF(name);
        out.writeInt(action);
        if (action == 0 || action == 2) {
            out.writeUTF(prefix);
            out.writeUTF(suffix);
            out.writeInt(options);
            out.writeUTF(visibility);
            out.writeUTF(collision);
            out.writeInt(color);
        }
        if (action == 0) {
            out.writeInt(players.size());
            for (String player : players) {
                out.writeUTF(player);
            }
        }
        return out;
    }
}
