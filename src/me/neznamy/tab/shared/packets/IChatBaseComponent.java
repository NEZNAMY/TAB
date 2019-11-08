package me.neznamy.tab.shared.packets;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import me.neznamy.tab.shared.ProtocolVersion;

@SuppressWarnings("unchecked")
public class IChatBaseComponent {

	private String text;
	private List<IChatBaseComponent> extras = new ArrayList<IChatBaseComponent>();
	private EnumChatFormat color;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;
	private ClickAction clickAction;
	private String clickValue;
	private HoverAction hoverAction;
	private String hoverValue;
	
	public IChatBaseComponent(String text) {
		this.text = text;
	}
	public void addExtra(IChatBaseComponent c) {
		extras.add(c);
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
	public IChatBaseComponent onClick(ClickAction action, String... value) {
		clickAction = action;
		String txt = "";
		for (int i=0; i<value.length; i++) {
			if (i>0) txt += "\n";
			txt += value[i];
		}
		this.clickValue = txt;
		return this;
	}
	public IChatBaseComponent onHover(HoverAction action, String... value) {
		hoverAction = action;
		String txt = "";
		for (int i=0; i<value.length; i++) {
			if (i>0) txt += "\n";
			txt += value[i];
		}
		this.hoverValue = txt;
		return this;
	}
	public String toString() {
		if (extras.isEmpty()) {
			if (text == null) return null;
			if (text.length() == 0) return "{\"translate\":\"\"}";
		}
		JSONObject json = new JSONObject();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
			//1.7+
			if (text != null && text.length() > 0) json.put("text", text);
			if (!extras.isEmpty()) json.put("extra", extras);
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
			if (!extras.isEmpty()) {
				for (IChatBaseComponent c : extras) {
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
	public enum ClickAction{
		OPEN_URL, OPEN_FILE, RUN_COMMAND, CHANGE_PAGE, SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //1.15
	}
	public enum HoverAction{
		SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY;
	}
}