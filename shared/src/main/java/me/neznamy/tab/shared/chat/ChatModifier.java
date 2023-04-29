package me.neznamy.tab.shared.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

@Data @NoArgsConstructor
public class ChatModifier {

    @Nullable private TextColor color;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;

    public ChatModifier(@NonNull ChatModifier modifier) {
        this.color = modifier.color == null ? null : new TextColor(modifier.color);
        this.bold = modifier.bold;
        this.italic = modifier.italic;
        this.underlined = modifier.underlined;
        this.strikethrough = modifier.strikethrough;
        this.obfuscated = modifier.obfuscated;
    }

    @SuppressWarnings("unchecked")
    public @NotNull JSONObject serialize(boolean rgbSupport) {
        JSONObject json = new JSONObject();
        if (color != null) json.put("color", color.toString(rgbSupport));
        if (bold) json.put("bold", true);
        if (italic) json.put("italic", true);
        if (underlined) json.put("underlined", true);
        if (strikethrough) json.put("strikethrough", true);
        if (obfuscated) json.put("obfuscated", true);
        return json;
    }

    public @NotNull String getMagicCodes() {
        StringBuilder builder = new StringBuilder();
        if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
        if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
        if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
        if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
        if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
        return builder.toString();
    }
}