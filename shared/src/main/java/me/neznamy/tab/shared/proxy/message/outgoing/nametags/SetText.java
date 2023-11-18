package me.neznamy.tab.shared.proxy.message.outgoing.nametags;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.proxy.message.outgoing.OutgoingMessage;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class SetText implements OutgoingMessage {

    private String line;
    private String text;

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("NameTagX");
        out.writeUTF("SetText");
        out.writeUTF(line);
        out.writeUTF(text);
        return out;
    }
}
