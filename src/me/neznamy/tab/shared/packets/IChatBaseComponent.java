package me.neznamy.tab.shared.packets;

import java.lang.reflect.Method;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class IChatBaseComponent {

	private static int serverVersion;
	private static Class<?> NBTTagCompound;
	private static Method CraftItemStack_asNMSCopy;
	private static Method ItemStack_save;
	
	static {
		try {
			String pack = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			serverVersion = Integer.parseInt(pack.split("_")[1]);
			NBTTagCompound = Class.forName("net.minecraft.server." + pack + ".NBTTagCompound");
			CraftItemStack_asNMSCopy = Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
			ItemStack_save = Class.forName("net.minecraft.server." + pack + ".ItemStack").getMethod("save", NBTTagCompound);
		} catch (Throwable t) {
			serverVersion = 14;
			//bungeecord, velocity
		}
	}
	private String text;
	private List<IChatBaseComponent> extras = new ArrayList<IChatBaseComponent>();
	private EnumChatFormat color;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;
	private ClickAction clickAction;
	private Object clickValue;
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
	
	
	
	
	
	public IChatBaseComponent onClickOpenUrl(String url) {
		clickAction = ClickAction.OPEN_URL;
		clickValue = url;
		return this;
	}
	public IChatBaseComponent onClickRunCommand(String command) {
		clickAction = ClickAction.RUN_COMMAND;
		clickValue = command;
		return this;
	}
	public IChatBaseComponent onClickSuggestCommand(String command) {
		clickAction = ClickAction.SUGGEST_COMMAND;
		clickValue = command;
		return this;
	}
	public IChatBaseComponent onClickChangePage(int newpage) {
		clickAction = ClickAction.CHANGE_PAGE;
		clickValue = newpage;
		return this;
	}
	
	
	
	
	
	public IChatBaseComponent onHoverShowText(String text) {
		hoverAction = HoverAction.SHOW_TEXT;
		hoverValue = text;
		return this;
	}
	public IChatBaseComponent onHoverShowItem(ItemStack item) {
		hoverAction = HoverAction.SHOW_ITEM;
//		hoverValue = CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()).toString();
		hoverValue = serialize(item);
		return this;
	}
	private String serialize(ItemStack item) {
		try {
			return ItemStack_save.invoke(CraftItemStack_asNMSCopy.invoke(null, item), NBTTagCompound.getConstructor().newInstance()).toString();
		} catch (Throwable t) {
			t.printStackTrace();
			return "null";
		}
	}
	public IChatBaseComponent onHoverShowEntity(UUID id, String customname, String type) {
		hoverAction = HoverAction.SHOW_ENTITY;
		String value = "{id:" + id.toString();
		if (type != null) value += ",type:" + type;
		if (customname != null) value += ",name:" + customname;
		hoverValue = (value += "}");
		return this;
	}
	
	
	
	
	
	public String toString() {
		if (extras.isEmpty()) {
			if (text == null) return null;
			if (text.length() == 0) return "{\"translate\":\"\"}";
		}
		JSONObject json = new JSONObject();
		if (serverVersion >= 7) {
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
			if (serverVersion == 6) {
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