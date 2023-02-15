package me.neznamy.tab.api.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.ChatClickable.EnumClickAction;
import me.neznamy.tab.api.chat.ChatHoverable.EnumHoverAction;
import org.json.simple.JSONObject;

import java.util.UUID;

@Data @NoArgsConstructor
public class ChatModifier {

    private TextColor color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ChatClickable clickEvent;
    private ChatHoverable hoverEvent;
    private String font;
    private ProtocolVersion targetVersion;

    public ChatModifier(@NonNull ChatModifier modifier) {
        this.color = modifier.color == null ? null : new TextColor(modifier.color);
        this.bold = modifier.bold;
        this.italic = modifier.italic;
        this.underlined = modifier.underlined;
        this.strikethrough = modifier.strikethrough;
        this.obfuscated = modifier.obfuscated;
        this.clickEvent = modifier.clickEvent == null ? null : new ChatClickable(modifier.clickEvent.getAction(), modifier.clickEvent.getValue());
        this.hoverEvent = modifier.hoverEvent == null ? null : new ChatHoverable(modifier.hoverEvent.getAction(), modifier.hoverEvent.getValue());
        this.font = modifier.font;
        this.targetVersion = modifier.targetVersion;
    }


    /**
     * Returns true if bold is defined and set to true, false otherwise
     *
     * @return  true if bold is defined and set to true, false otherwise
     */
    public boolean isBold() {
        return Boolean.TRUE.equals(bold);
    }

    /**
     * Returns true if italic is defined and set to true, false otherwise
     *
     * @return  true if italic is defined and set to true, false otherwise
     */
    public boolean isItalic() {
        return Boolean.TRUE.equals(italic);
    }

    /**
     * Returns true if underlined is defined and set to true, false otherwise
     *
     * @return  true if underlined is defined and set to true, false otherwise
     */
    public boolean isUnderlined() {
        return Boolean.TRUE.equals(underlined);
    }

    /**
     * Returns true if strikethrough is defined and set to true, false otherwise
     *
     * @return  true if strikethrough is defined and set to true, false otherwise
     */
    public boolean isStrikethrough() {
        return Boolean.TRUE.equals(strikethrough);
    }

    /**
     * Returns true if obfuscation is defined and set to true, false otherwise
     *
     * @return  true if obfuscation is defined and set to true, false otherwise
     */
    public boolean isObfuscated() {
        return Boolean.TRUE.equals(obfuscated);
    }

    /**
     * Sets click action to OPEN_URL and url to given value
     *
     * @param   url
     *          url to open
     */
    public void onClickOpenUrl(@NonNull String url) {
        clickEvent = new ChatClickable(EnumClickAction.OPEN_URL, url);
    }

    /**
     * Sets click action to RUN_COMMAND and command to given value
     *
     * @param   command
     *          command to perform, might be without / to send a chat message
     */
    public void onClickRunCommand(@NonNull String command) {
        clickEvent = new ChatClickable(EnumClickAction.RUN_COMMAND, command);
    }

    /**
     * Sets click action to SUGGEST_COMMAND and command to given value
     *
     * @param   command
     *          command to suggest
     */
    public void onClickSuggestCommand(@NonNull String command) {
        clickEvent = new ChatClickable(EnumClickAction.SUGGEST_COMMAND, command);
    }

    /**
     * Sets click action to CHANGE_PAGE and page id to given value
     *
     * @param   newPage
     *          id of new page
     */
    public void onClickChangePage(int newPage) {
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() < 8) throw new UnsupportedOperationException("change_page click action is not supported on <1.8");
        clickEvent = new ChatClickable(EnumClickAction.CHANGE_PAGE, String.valueOf(newPage));
    }

    /**
     * Sets click action to COPY_TO_CLIPBOARD and text to provided value
     *
     * @param   text
     *          text to copy to clipboard on click
     */
    public void onClickCopyToClipBoard(@NonNull String text) {
        clickEvent = new ChatClickable(EnumClickAction.COPY_TO_CLIPBOARD, text);
    }

    public void onClick(@NonNull EnumClickAction action, @NonNull String value) {
        clickEvent = new ChatClickable(action, value);
    }

    /**
     * Sets hover action to SHOW_TEXT and text to given value
     *
     * @param   text
     *          text to show
     */
    public void onHoverShowText(@NonNull IChatBaseComponent text) {
        hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_TEXT, text);
    }

    /**
     * Sets hover action to SHOW_ITEM and item to given value
     *
     * @param   serializedItem
     *          item to show
     */
    public void onHoverShowItem(@NonNull String serializedItem) {
        hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ITEM, new IChatBaseComponent(serializedItem));
    }

    /**
     * Sets hover action to SHOW_ENTITY and entity data to given values
     *
     * @param   type
     *          entity type
     * @param   id
     *          entity uuid
     * @param   name
     *          entity custom name
     */
    public void onHoverShowEntity(@NonNull String type, @NonNull UUID id, String name) {
        hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ENTITY, new ChatComponentEntity(type, id, name));
    }

    public void onHover(@NonNull EnumHoverAction action, @NonNull IChatBaseComponent value) {
        hoverEvent = new ChatHoverable(action, value);
    }

    @SuppressWarnings("unchecked")
    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        if (color != null) json.put("color", targetVersion.getMinorVersion() >= 16 ? color.toString() : color.getLegacyColor().toString().toLowerCase());
        if (bold != null) json.put("bold", bold);
        if (italic != null) json.put("italic", italic);
        if (underlined != null) json.put("underlined", underlined);
        if (strikethrough != null) json.put("strikethrough", strikethrough);
        if (obfuscated != null) json.put("obfuscated", obfuscated);
        if (clickEvent != null) {
            JSONObject click = new JSONObject();
            click.put("action", clickEvent.getAction().toString().toLowerCase());
            click.put("value", clickEvent.getValue());
            json.put("clickEvent", click);
        }
        if (hoverEvent != null) {
            JSONObject hover = new JSONObject();
            hover.put("action", hoverEvent.getAction().toString().toLowerCase());
            if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 16) {
                hover.put(hoverEvent.getAction().getPreferredKey(), hoverEvent.getValue());
            } else {
                hover.put("value", TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9 ?
                        hoverEvent.getValue() : hoverEvent.getValue().toRawText());
            }
            json.put("hoverEvent", hover);
        }
        if (font != null) json.put("font", font);
        return json;
    }

    public String getMagicCodes() {
        StringBuilder builder = new StringBuilder();
        if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
        if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
        if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
        if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
        if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
        return builder.toString();
    }
}