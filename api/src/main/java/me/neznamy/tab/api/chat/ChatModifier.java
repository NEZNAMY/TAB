package me.neznamy.tab.api.chat;

import java.util.UUID;

import org.json.simple.JSONObject;

import me.neznamy.tab.api.chat.ChatClickable.EnumClickAction;
import me.neznamy.tab.api.chat.rgb.ChatHoverable;
import me.neznamy.tab.api.chat.rgb.ChatHoverable.EnumHoverAction;

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
	
	public ChatModifier() {
	}
	
	public ChatModifier(ChatModifier modifier) {
		this.bold = modifier.bold;
		this.italic = modifier.italic;
		this.underlined = modifier.underlined;
		this.strikethrough = modifier.strikethrough;
		this.obfuscated = modifier.obfuscated;
		this.clickEvent = modifier.clickEvent == null ? null : new ChatClickable(modifier.clickEvent.getAction(), modifier.clickEvent.getValue());
		this.hoverEvent = modifier.hoverEvent == null ? null : new ChatHoverable(modifier.hoverEvent.getAction(), modifier.hoverEvent.getValue());
		this.font = modifier.font;
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
	 * @return self
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
	 * @param newpage - id of new page
	 */
	public void onClickChangePage(int newpage) {
		clickEvent = new ChatClickable(EnumClickAction.CHANGE_PAGE, String.valueOf(newpage));
	}
	
	public void onClick(EnumClickAction action, String value) {
		clickEvent = new ChatClickable(action, value);
	}

	/**
	 * Sets hover action to SHOW_TEXT and text to given value
	 * @param text - text to show
	 */
	public void onHoverShowText(String text) {
		onHoverShowText(IChatBaseComponent.optimizedComponent(text));
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
		hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ITEM, serializedItem);
	}

	/**
	 * Sets hover action to SHOW_ENTITY and entity data to given values
	 * @param id - entity uuid
	 * @param customname - entity custom name, can be null
	 * @param type - entity type, can be null
	 */
	public void onHoverShowEntity(UUID id, String type, String customname) {
		hoverEvent = new ChatHoverable(EnumHoverAction.SHOW_ENTITY, String.format("{id:%s,type:%s,name:%s}", id, type, customname));
	}
	
	public void onHover(EnumHoverAction action, Object value) {
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
		if (color != null) json.put("color", color.toString());
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
			hover.put("value", hoverEvent.getValue());
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