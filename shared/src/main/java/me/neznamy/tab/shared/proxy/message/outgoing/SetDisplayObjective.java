package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class SetDisplayObjective implements OutgoingMessage {

    private Scoreboard.DisplaySlot slot;
    private String objective;

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PacketPlayOutScoreboardDisplayObjective");
        out.writeInt(slot.ordinal());
        out.writeUTF(objective);
        return out;
    }
}
