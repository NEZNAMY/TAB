package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class PlayerJoin implements OutgoingMessage {

    private int protocolVersion;
    private boolean forwardGroup;
    private Map<String, Integer> placeholders;
    private Map<String, Map<Object, Object>> replacements;
    private UnlimitedNametagSettings unlimitedNameTags;

    @Override
    @NotNull
    public ByteArrayDataOutput write() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerJoin");
        out.writeInt(protocolVersion);
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
        out.writeBoolean(unlimitedNameTags != null);
        if (unlimitedNameTags != null) {
            out.writeBoolean(unlimitedNameTags.disableOnBoats);
            out.writeBoolean(unlimitedNameTags.alwaysVisible);
            out.writeBoolean(unlimitedNameTags.isDisabledForPlayer);
            out.writeInt(unlimitedNameTags.dynamicLines.size());
            for (String line : unlimitedNameTags.dynamicLines) {
                out.writeUTF(line);
            }
            out.writeInt(unlimitedNameTags.staticLines.size());
            for (Map.Entry<String, Object> entry : unlimitedNameTags.staticLines.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeDouble(Double.parseDouble(String.valueOf(entry.getValue())));
            }
        }
        return out;
    }

    @AllArgsConstructor
    public static class UnlimitedNametagSettings {

        private boolean disableOnBoats;
        private boolean alwaysVisible;
        private boolean isDisabledForPlayer;
        private List<String> dynamicLines;
        private Map<String, Object> staticLines;
    }
}
