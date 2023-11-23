package me.neznamy.tab.shared.proxy.message.outgoing.nametags;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.neznamy.tab.shared.proxy.message.outgoing.OutgoingMessage;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class Destroy implements OutgoingMessage {

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("NameTagX");
        out.writeUTF("Destroy");
        return out;
    }
}