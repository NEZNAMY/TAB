package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class SetScore implements OutgoingMessage {

    private String objective;
    private int action;
    private String scoreHolder;
    private int score;
    private String displayName;
    private String numberFormat;

    public SetScore(String objective, String scoreHolder) {
        this.objective = objective;
        this.scoreHolder = scoreHolder;
        action = Scoreboard.ScoreAction.REMOVE;
    }

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PacketPlayOutScoreboardScore");
        out.writeUTF(objective);
        out.writeInt(action);
        out.writeUTF(scoreHolder);
        if (action == 0) {
            out.writeInt(score);
            out.writeBoolean(displayName != null);
            if (displayName != null) out.writeUTF(displayName);
            out.writeBoolean(numberFormat != null);
            if (numberFormat != null) out.writeUTF(numberFormat);
        }
        return out;
    }
}
