package me.neznamy.tab.shared.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
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
    @Nullable private ClickEvent clickEvent;
    @Nullable private String font;

    public ChatModifier(@NotNull ChatModifier modifier) {
        if (modifier.color != null) color = new TextColor(modifier.color);
        bold = modifier.bold;
        italic = modifier.italic;
        underlined = modifier.underlined;
        strikethrough = modifier.strikethrough;
        obfuscated = modifier.obfuscated;
        if (modifier.clickEvent != null) clickEvent = new ClickEvent(modifier.clickEvent.getAction(), modifier.clickEvent.getValue());
        font = modifier.font;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public JSONObject serialize(boolean rgbSupport) {
        JSONObject json = new JSONObject();
        if (color != null) json.put("color", color.toString(rgbSupport));
        if (bold) json.put("bold", true);
        if (italic) json.put("italic", true);
        if (underlined) json.put("underlined", true);
        if (strikethrough) json.put("strikethrough", true);
        if (obfuscated) json.put("obfuscated", true);
        if (clickEvent != null) {
            JSONObject click = new JSONObject();
            click.put("action", clickEvent.getAction().name().toLowerCase());
            click.put("value", clickEvent.getValue());
            json.put("clickEvent", click);
        }
        if (font != null) json.put("font", font);
        return json;
    }

    /**
     * Returns a String consisting of magic codes (color symbol + character) of
     * each magic code used. If none are used, empty String is returned.
     *
     * @return  Magic codes of this modifier as String
     */
    @NotNull
    public String getMagicCodes() {
        StringBuilder builder = new StringBuilder();
        if (bold) builder.append(EnumChatFormat.BOLD.getFormat());
        if (italic) builder.append(EnumChatFormat.ITALIC.getFormat());
        if (underlined) builder.append(EnumChatFormat.UNDERLINE.getFormat());
        if (strikethrough) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
        if (obfuscated) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
        return builder.toString();
    }
}