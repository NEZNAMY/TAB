package me.neznamy.tab.shared.packets;

import java.util.*;

import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

@SuppressWarnings("unchecked")
public class IChatBaseComponent {

	public static final String EMPTY_COMPONENT = "{\"signature\":\"TAB\",\"translate\":\"\"}";

	private String text;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;
	private EnumChatFormat color;
	private ClickAction clickAction;
	private Object clickValue;
	private HoverAction hoverAction;
	private String hoverValue;
	private List<IChatBaseComponent> extra;

	private JSONObject jsonObject = new JSONObject();

	public IChatBaseComponent() {
		jsonObject.put("signature", "TAB");
	}
	public IChatBaseComponent(String text) {
		this.text = text;
		jsonObject.put("text", text);
		jsonObject.put("signature", "TAB");
	}

	public List<IChatBaseComponent> getExtra(){
		return extra;
	}
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
		this.extra = components;
		jsonObject.put("extra", extra);
		return this;
	}
	public void addExtra(IChatBaseComponent child) {
		if (extra == null) {
			extra = new ArrayList<IChatBaseComponent>();
			jsonObject.put("extra", extra);
		}
		extra.add(child);
	}




	public String getText() {
		return text;
	}
	public EnumChatFormat getColor() {
		return color;
	}
	public boolean isBold(){
		return bold == null ? false : bold;
	}
	public boolean isItalic(){
		return italic == null ? false : italic;
	}
	public boolean isUnderlined(){
		return underlined == null ? false : underlined;
	}
	public boolean isStrikethrough(){
		return strikethrough == null ? false : strikethrough;
	}
	public boolean isObfuscated(){
		return obfuscated == null ? false : obfuscated;
	}

	public IChatBaseComponent setText(String text) {
		this.text = text;
		jsonObject.put("text", text);
		return this;
	}
	public IChatBaseComponent setColor(EnumChatFormat color) {
		this.color = color;
		jsonObject.put("color", color.toString().toLowerCase());
		return this;
	}
	public IChatBaseComponent setBold(Boolean bold) {
		this.bold = bold;
		jsonObject.put("bold", bold);
		return this;
	}
	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		jsonObject.put("italic", italic);
		return this;
	}
	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		jsonObject.put("underlined", underlined);
		return this;
	}
	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		jsonObject.put("strikethrough", strikethrough);
		return this;
	}
	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
		jsonObject.put("obfuscated", obfuscated);
		this.obfuscated = obfuscated;
		return this;
	}

	public IChatBaseComponent onClickOpenUrl(String url) {
		return onClick(ClickAction.OPEN_URL, url);
	}
	public IChatBaseComponent onClickRunCommand(String command) {
		return onClick(ClickAction.RUN_COMMAND, command);
	}
	public IChatBaseComponent onClickSuggestCommand(String command) {
		return onClick(ClickAction.SUGGEST_COMMAND, command);
	}
	public IChatBaseComponent onClickChangePage(int newpage) {
		return onClick(ClickAction.CHANGE_PAGE, newpage);
	}
	private IChatBaseComponent onClick(ClickAction action, Object value) {
		clickAction = action;
		clickValue = value;
		JSONObject click = new JSONObject();
		click.put("action", action.toString().toLowerCase());
		click.put("value", value);
		jsonObject.put("clickEvent", click);
		return this;
	}

	public IChatBaseComponent onHoverShowText(String text) {
		return onHover(HoverAction.SHOW_TEXT, text);
	}
	public IChatBaseComponent onHoverShowItem(ItemStack item) {
		return onHover(HoverAction.SHOW_ITEM, MethodAPI.getInstance().serialize(item));
	}
	public IChatBaseComponent onHoverShowEntity(UUID id, String customname, String type) {
		JSONObject json = new JSONObject();
		json.put("id", id.toString());
		if (type != null) json.put("type", type);
		if (customname != null) json.put("name", customname);
		return onHover(HoverAction.SHOW_ENTITY, json.toString());
	}
	private IChatBaseComponent onHover(HoverAction action, String value) {
		hoverAction = action;
		hoverValue = value;
		JSONObject hover = new JSONObject();
		hover.put("action", action.toString().toLowerCase());
		hover.put("value", value);
		jsonObject.put("hoverEvent", hover);
		return this;
	}





	public String toString() {
		if (extra == null) {
			if (text == null) return null;
			if (text.length() == 0) return EMPTY_COMPONENT;
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
			//1.7+
			if (text == null) {
				//com.google.gson.JsonParseException: Don't know how to turn XXX into a Component
				jsonObject.put("text", "");
			}
			return jsonObject.toString();
		} else {
			String text = "";
			if (this.text != null) text += this.text;
			if (!extra.isEmpty()) {
				for (IChatBaseComponent c : extra) {
					if (c.text != null) text += c.text;
				}
			}
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 6) {
				//1.6.x
				jsonObject.put("text", text);
				return jsonObject.toString();
			} else {
				//1.5.x
				return text;
			}
		}
	}

	public static IChatBaseComponent fromColoredText(String message){
		List<IChatBaseComponent> components = new ArrayList<IChatBaseComponent>();
		StringBuilder builder = new StringBuilder();
		IChatBaseComponent component = new IChatBaseComponent();
		for (int i = 0; i < message.length(); i++){
			char c = message.charAt(i);
			if (c == '§'){
				i++;
				if (i >= message.length()) {
					break;
				}
				c = message.charAt(i);
				if ((c >= 'A') && (c <= 'Z')) {
					c = (char)(c + ' ');
				}
				EnumChatFormat format = EnumChatFormat.getByChar(c);
				if (format != null){
					if (builder.length() > 0){
						IChatBaseComponent old = component;
						component = old.clone();
						old.setText(builder.toString());
						builder = new StringBuilder();
						components.add(old);
					}
					switch (format){
					case BOLD: 
						component.setBold(true);
						break;
					case ITALIC: 
						component.setItalic(true);
						break;
					case UNDERLINE: 
						component.setUnderlined(true);
						break;
					case STRIKETHROUGH: 
						component.setStrikethrough(true);
						break;
					case OBFUSCATED: 
						component.setObfuscated(true);
						break;
					case RESET: 
						format = EnumChatFormat.WHITE;
					default: 
						component = new IChatBaseComponent();
						component.setColor(format);
						break;
					}
				}
			} else {
				int pos = message.indexOf(' ', i);
				if (pos == -1) {
					pos = message.length();
				}
				builder.append(c);
			}
		}
		component.setText(builder.toString());
		components.add(component);

		return new IChatBaseComponent().setExtra(components);
	}

	public IChatBaseComponent clone() {
		IChatBaseComponent copy = new IChatBaseComponent(text);
		copy.bold = bold;
		copy.clickAction = clickAction;
		copy.clickValue = clickValue;
		copy.color = color;
//		copy.extra = extra;
		copy.hoverAction = hoverAction;
		copy.hoverValue = hoverValue;
		copy.italic = italic;
		copy.obfuscated = obfuscated;
		copy.strikethrough = strikethrough;
		copy.underlined = underlined;
		return copy;
	}

	public enum ClickAction{
		OPEN_URL,
		@Deprecated OPEN_FILE,//Cannot be sent by server
		RUN_COMMAND,
		@Deprecated TWITCH_USER_INFO, //Removed in 1.9
		CHANGE_PAGE,
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //since 1.15
	}
	public enum HoverAction{
		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY,
		@Deprecated SHOW_ACHIEVEMENT;//Removed in 1.12
	}
}