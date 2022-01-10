package me.neznamy.tab.api.chat;

import java.util.UUID;

import org.json.simple.JSONObject;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.ChatClickable.EnumClickAction;
import me.neznamy.tab.api.chat.ChatHoverable.EnumHoverAction;

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
	
	public ChatModifier() {
	}
	
	public ChatModifier(ChatModifier modifier) {
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

	public TextColor getColor() {
		return color;
	}
	
	public void setColor(TextColor color) {
		this.color = color;
	}

	public Boolean getBold() {
		return bold;
	}

	public void setBold(Boolean bold) {
		this.bold = bold;
	}

	public Boolean getItalic() {
		return italic;
	}

	public void setItalic(Boolean italic) {
		this.italic = italic;
	}

	public Boolean getUnderlined() {
		return underlined;
	}

	public void setUnderlined(Boolean underlined) {
		this.underlined = underlined;
	}

	public Boolean getStrikethrough() {
		return strikethrough;
	}

	public void setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
	}

	public Boolean getObfuscated() {
		return obfuscated;
	}

	public void setObfuscated(Boolean obfuscated) {
		this.obfuscated = obfuscated;
	}
	
	/**
	 * Returns true if bold is defined and set to true, false otherwise
	 * @return true if bold is defined and set to true, false otherwise
	 */
	public boolean isBold(){
		return Boolean.TRUE.equals(bold);
	}

	/**
	 * Returns true if italic is defined and set to true, false otherwise
	 * @return true if italic is defined and set to true, false otherwise
	 */
	public boolean isItalic(){
		return Boolean.TRUE.equals(italic);
	}

	/**
	 * Returns true if underlined is defined and set to true, false otherwise
	 * @return true if underlined is defined and set to true, false otherwise
	 */
	public boolean isUnderlined(){
		return Boolean.TRUE.equals(underlined);
	}

	/**
	 * Returns true if strikethrough is defined and set to true, false otherwise
	 * @return true if strikethrough is defined and set to true, false otherwise
	 */
	public boolean isStrikethrough(){
		return Boolean.TRUE.equals(strikethrough);
	}

	/**
	 * Returns true if obfuscation is defined and set to true, false otherwise
	 * @return true if obfuscation is defined and set to true, false otherwise
	 */
	public boolean isObfuscated(){
		return Boolean.TRUE.equals(obfuscated);
	}

	public ChatClickable getClickEvent() {
		return clickEvent;
	}

	public void setClickEvent(ChatClickable clickEvent) {
		this.clickEvent = clickEvent;
	}

	public ChatHoverable getHoverEvent() {
		return hoverEvent;
	}

	public void setHoverEvent(ChatHoverable hoverEvent) {
		this.hoverEvent = hoverEvent;
	}
	
	/**
	 * Sets click action to OPEN_URL and url to given value
	 * @param url - url to open
	 */
	public void onClickOpenUrl(String url) {
		clickEvent = new ChatClickable(EnumClickAction.OPEN_URL, url);
	}

	/**
	 * Sets click action to RUN_COMMAND and command to given value
	 * @param command - command to perform, might be without / to send a chat message
	 */
	public void onClickRunCommand(String command) {
		clickEvent = new ChatClickable(EnumClickAction.RUN_COMMAND, command);
	}

	/**
	 * Sets click action to SUGGEST_COMMAND and command to given value
	 * @param command - command to suggest
	 */
	public void onClickSuggestCommand(String command) {
		clickEvent = new ChatClickable(EnumClickAction.SUGGEST_COMMAND, command);
	}

	/**
	 * Sets click action to CHANGE_PAGE and page id to given value
	 * @param newPage - id of new page
	 */
	public void onClickChangePage(int newPage) {
		if (TabAPI.getInstance().getServerVersion().getMinorVersion() < 8) throw new UnsupportedOperationException("change_page click action is not supported on <1.8");
		clickEvent = new ChatClickable(EnumClickAction.CHANGE_PAGE, String.valueOf(newPage));
	}

	/**
	 * Sets click action to COPY_TO_CLIPBOARD and text to provided value
	 * @param text - text to copy to clipboard on click
	 */
	public void onClickCopyToClipBoard(String text) {
		clickEvent = new ChatClickable(EnumClickAction.COPY_TO_CLIPBOARD, text);
	}
	
	public void onClick(EnumClickAction action, String value) {
		clickEvent = new ChatClickable(action, value);
	}

	/**
	 * Sets hover action to SHOW_TEXT and text to given value
	 * @param text - text to show
	 */
	public void onHoverShowText(IChatBaseComponent text) {
		hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_TEXT, text);
	}

	/**
	 * Sets hover action to SHOW_ITEM and item to given value
	 * @param serializedItem - item to show
	 */
	public void onHoverShowItem(String serializedItem) {
		hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ITEM, new IChatBaseComponent(serializedItem));
	}

	/**
	 * Sets hover action to SHOW_ENTITY and entity data to given values
	 * @param type - entity type
	 * @param id - entity uuid
	 * @param name - entity custom name
	 */
	public void onHoverShowEntity(String type, UUID id, String name) {
		if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 16) {
			hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ENTITY, new ChatComponentEntity(type, id, name));
		} else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 13) {
			hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ENTITY, new IChatBaseComponent(String.format("{type:\"%s\",id:\"%s\",name:\"{\\\"text\\\":\\\"%s\\\"}\"}", type, id, name)));
		} else if (TabAPI.getInstance().getServerVersion().getMinorVersion() == 12) {
			hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ENTITY, new IChatBaseComponent(String.format("{type:\"%s\",id:\"%s\",name:\"%s\"}", type, id, name)));
		} else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 8) {
			hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ENTITY, new IChatBaseComponent(String.format("{type:%s,id:%s,name:%s}", type, id, name)));
		} else {
			throw new IllegalStateException("show_entity hover action is not supported on <1.8");
		}
	}
	
	public void onHover(EnumHoverAction action, IChatBaseComponent value) {
		hoverEvent = new ChatHoverable(action, value);
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
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
				hover.put("contents", hoverEvent.getValue());
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
	
	public void setTargetVersion(ProtocolVersion targetVersion) {
		this.targetVersion = targetVersion;
	}
	
	public ProtocolVersion getTargetVersion() {
		return targetVersion;
	}
}