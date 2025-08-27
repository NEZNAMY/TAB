package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class PlayerJoin implements OutgoingMessage {

    private boolean forwardGroup;
    private Map<String, Integer> placeholders;
    private Map<String, Map<Object, Object>> replacements;

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerJoin");
        out.writeInt(0); // Protocol version, which is no longer used, write to not have to break protocol
        out.writeBoolean(forwardGroup);
        out.writeInt(placeholders.size());
        for (Map.Entry<String, Integer> entry : placeholders.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeInt(entry.getValue());
        }
        out.writeInt(replacements.size());
        for (Map.Entry<String, Map<Object, Object>> entry : replacements.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeInt(entry.getValue().size());
            for (Map.Entry<Object, Object> rule : entry.getValue().entrySet()) {
                out.writeUTF(EnumChatFormat.color(String.valueOf(rule.getKey())));
                out.writeUTF(EnumChatFormat.color(String.valueOf(rule.getValue())));
            }
        }
        out.writeBoolean(false); // Deleted unlimited nametags
        return out;
    }
}
