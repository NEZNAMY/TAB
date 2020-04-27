package me.neznamy.tab.shared.packets;

import java.util.*;

import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;

@SuppressWarnings("unchecked")
public class IChatBaseComponent {

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
	private IChatBaseComponent parent;
	private List<IChatBaseComponent> extra;


	public IChatBaseComponent() {
	}
	public IChatBaseComponent(String text) {
		this.text = text;
	}

	public List<IChatBaseComponent> getExtra(){
		return extra;
	}
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
		for (IChatBaseComponent component : components) {
			component.parent = this;
		}
		this.extra = components;
		return this;
	}
	public void addExtra(IChatBaseComponent child) {
		if (extra == null) extra = new ArrayList<IChatBaseComponent>();
		extra.add(child);
		child.parent = this;
	}




	public String getText() {
		return text;
	}
	public EnumChatFormat getColor() {
		if (color != null) return color;
		return parent.color;
	}
	public boolean isBold(){
		if (bold == null) {
			return (parent != null) && (parent.isBold());
		}
		return bold;
	}
	public boolean isItalic(){
		if (italic == null) {
			return (parent != null) && (parent.isItalic());
		}
		return italic;
	}
	public boolean isUnderlined(){
		if (underlined == null) {
			return (parent != null) && (parent.isUnderlined());
		}
		return underlined;
	}
	public boolean isStrikethrough(){
		if (strikethrough == null) {
			return (parent != null) && (parent.isStrikethrough());
		}
		return strikethrough;
	}
	public boolean isObfuscated(){
		if (obfuscated == null) {
			return (parent != null) && (parent.isObfuscated());
		}
		return obfuscated;
	}

	public IChatBaseComponent setText(String text) {
		this.text = text;
		return this;
	}
	public IChatBaseComponent setColor(EnumChatFormat color) {
		this.color = color;
		return this;
	}
	public IChatBaseComponent setBold(Boolean bold) {
		this.bold = bold;
		return this;
	}
	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		return this;
	}
	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		return this;
	}
	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		return this;
	}
	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
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
		return this;
	}





	public IChatBaseComponent onHoverShowText(String text) {
		return onHover(HoverAction.SHOW_TEXT, text);
	}
	public IChatBaseComponent onHoverShowItem(ItemStack item) {
		return onHover(HoverAction.SHOW_ITEM, MethodAPI.getInstance().serialize(item));
	}
	public IChatBaseComponent onHoverShowEntity(UUID id, String customname, String type) {
		String value = "{id:" + id.toString();
		if (type != null) value += ",type:" + type;
		if (customname != null) value += ",name:" + customname;
		return onHover(HoverAction.SHOW_ENTITY, value + "}");
	}
	private IChatBaseComponent onHover(HoverAction action, String value) {
		hoverAction = action;
		hoverValue = value;
		return this;
	}





	public String toString() {
		if (extra == null) {
			if (text == null) return null;
			if (text.length() == 0) return "{\"translate\":\"\"}";
		}
		JSONObject json = new JSONObject();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
			//1.7+
			if (text != null) {
				json.put("text", text);
			} else {
				json.put("text", "");
			}
			if (extra != null) json.put("extra", extra);
			if (color != null) json.put("color", color.toString().toLowerCase());
			if (bold != null) json.put("bold", bold);
			if (italic != null) json.put("italic", italic);
			if (underlined != null) json.put("underlined", underlined);
			if (strikethrough != null) json.put("strikethrough", strikethrough);
			if (obfuscated != null) json.put("obfuscated", obfuscated);
			if (clickAction != null) {
				JSONObject o = new JSONObject();
				o.put("action", clickAction.toString().toLowerCase());
				o.put("value", clickValue);
				json.put("clickEvent", o);
			}
			if (hoverAction != null) {
				JSONObject o = new JSONObject();
				o.put("action", hoverAction.toString().toLowerCase());
				o.put("value", hoverValue);
				json.put("hoverEvent", o);
			}
			return json.toString();
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
				json.put("text", text);
				return json.toString();
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
		IChatBaseComponent copy = new IChatBaseComponent();
		copy.bold = bold;
		copy.clickAction = clickAction;
		copy.clickValue = clickValue;
		copy.color = color;
//		copy.extra = extra;
		copy.hoverAction = hoverAction;
		copy.hoverValue = hoverValue;
		copy.italic = italic;
		copy.obfuscated = obfuscated;
//		copy.parent = parent;
		copy.strikethrough = strikethrough;
		copy.text = text;
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