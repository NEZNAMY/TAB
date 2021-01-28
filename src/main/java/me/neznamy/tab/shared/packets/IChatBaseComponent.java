package me.neznamy.tab.shared.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.rgb.RGBUtils;
import me.neznamy.tab.shared.rgb.TextColor;

/**
 * A class representing the n.m.s.IChatBaseComponent class to make work with it much easier
 */
@SuppressWarnings("unchecked")
public class IChatBaseComponent {

	private final String EMPTY_TRANSLATABLE = "{\"translate\":\"\"}";
	private final String EMPTY_TEXT = "{\"text\":\"\"}";
	private static final RGBUtils rgb = new RGBUtils();

	private String text;

	private TextColor color;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;

	private ClickAction clickAction;
	private Object clickValue;
	private HoverAction hoverAction;
	private String hoverValue;

	private List<IChatBaseComponent> extra;
	private JSONObject jsonObject = new JSONObject();

	/**
	 * Constructs a new empty component
	 */
	public IChatBaseComponent() {
	}
	
	/**
	 * Constructs new instance with given text
	 * @param text - text to display
	 */
	public IChatBaseComponent(String text) {
		setText(text);
	}

	/**
	 * Returns list of extra components or null if none are defined
	 * @return list of extras
	 */
	public List<IChatBaseComponent> getExtra(){
		return extra;
	}
	
	/**
	 * Sets full list of extra components to given list
	 * @param components - components to use as extra
	 * @return self
	 */
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
		this.extra = components;
		jsonObject.put("extra", extra);
		return this;
	}
	
	/**
	 * Appends provided component as extra component
	 * @param child - component to append
	 * @return self
	 */
	public IChatBaseComponent addExtra(IChatBaseComponent child) {
		if (extra == null) {
			extra = new ArrayList<IChatBaseComponent>();
			jsonObject.put("extra", extra);
		}
		extra.add(child);
		return this;
	}

	/**
	 * Returns text of this component
	 * @return text of this component
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Returns color of the text or null if not set
	 * @return color or null if not set
	 */
	public TextColor getColor() {
		return color;
	}
	
	/**
	 * Returns true if bold is defined and set to true, false otherwise
	 * @return true if bold is defined and set to true, false otherwise
	 */
	public boolean isBold(){
		return bold == null ? false : bold;
	}
	
	/**
	 * Returns true if italic is defined and set to true, false otherwise
	 * @return true if italic is defined and set to true, false otherwise
	 */
	public boolean isItalic(){
		return italic == null ? false : italic;
	}
	
	/**
	 * Returns true if underlined is defined and set to true, false otherwise
	 * @return true if underlined is defined and set to true, false otherwise
	 */
	public boolean isUnderlined(){
		return underlined == null ? false : underlined;
	}
	
	/**
	 * Returns true if strikethrough is defined and set to true, false otherwise
	 * @return true if strikethrough is defined and set to true, false otherwise
	 */
	public boolean isStrikethrough(){
		return strikethrough == null ? false : strikethrough;
	}
	
	/**
	 * Returns true if obfuscation is defined and set to true, false otherwise
	 * @return true if obfuscation is defined and set to true, false otherwise
	 */
	public boolean isObfuscated(){
		return obfuscated == null ? false : obfuscated;
	}

	/**
	 * Sets text of this component
	 * @param text - text to show
	 * @return self
	 */
	public IChatBaseComponent setText(String text) {
		this.text = text;
		if (text != null) {
			jsonObject.put("text", text);
		} else {
			jsonObject.remove("text");
		}
		return this;
	}
	
	/**
	 * Sets color of text
	 * @param color - text color
	 * @return self
	 */
	public IChatBaseComponent setColor(TextColor color) {
		this.color = color;
		return this;
	}
	
	/**
	 * Sets bold status to requested value
	 * @param bold - true if bold, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setBold(Boolean bold) {
		this.bold = bold;
		if (bold != null) {
			jsonObject.put("bold", bold);
		} else {
			jsonObject.remove("bold");
		}
		return this;
	}
	
	/**
	 * Sets italic status to requested value
	 * @param italic - true if italic, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		if (italic != null) {
			jsonObject.put("italic", italic);
		} else {
			jsonObject.remove("italic");
		}
		return this;
	}
	
	/**
	 * Sets underline status to requested value
	 * @param underlined - true if underlined, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		if (underlined != null) {
			jsonObject.put("underlined", underlined);
		} else {
			jsonObject.remove("underlined");
		}
		return this;
	}
	
	/**
	 * Sets strikethrough status to requested value
	 * @param strikethrough - true if strikethrough, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		if (strikethrough != null) {
			jsonObject.put("strikethrough", strikethrough);
		} else {
			jsonObject.remove("strikethrough");
		}
		return this;
	}
	
	/**
	 * Sets obfuscation status to requested value
	 * @param obfuscated - true if obfuscated, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
		this.obfuscated = obfuscated;
		if (obfuscated != null) {
			jsonObject.put("obfuscated", obfuscated);
		} else {
			jsonObject.remove("obfuscated");
		}
		return this;
	}

	/**
	 * Returns click action or null if not set
	 * @return click action
	 */
	public ClickAction getClickAction() {
		return clickAction;
	}

	/**
	 * Returns click value or null if not set
	 * @return click value
	 */
	public Object getClickValue() {
		return clickValue;
	}

	/**
	 * Sets click action to OPEN_URL and url to given value
	 * @param url - url to open
	 * @return self
	 */
	public IChatBaseComponent onClickOpenUrl(String url) {
		return onClick(ClickAction.OPEN_URL, url);
	}

	/**
	 * Sets click action to RUN_COMMAND and command to given value
	 * @param command - command to perform, might be without / to send a chat message
	 * @return self
	 */
	public IChatBaseComponent onClickRunCommand(String command) {
		return onClick(ClickAction.RUN_COMMAND, command);
	}

	/**
	 * Sets click action to SUGGEST_COMMAND and command to given value
	 * @param command - command to suggest
	 * @return self
	 */
	public IChatBaseComponent onClickSuggestCommand(String command) {
		return onClick(ClickAction.SUGGEST_COMMAND, command);
	}

	/**
	 * Sets click action to CHANGE_PAGE and page id to given value
	 * @param newpage - id of new page
	 * @return self
	 */
	public IChatBaseComponent onClickChangePage(int newpage) {
		return onClick(ClickAction.CHANGE_PAGE, newpage);
	}

	/**
	 * Sets click action and value to given values
	 * @param action - action to perform on click
	 * @param value - value to perform action with
	 * @return self
	 */
	private IChatBaseComponent onClick(ClickAction action, Object value) {
		clickAction = action;
		clickValue = value;
		JSONObject click = new JSONObject();
		click.put("action", action.toString().toLowerCase());
		click.put("value", value);
		jsonObject.put("clickEvent", click);
		return this;
	}

	/**
	 * Returns hover action or null if not set
	 * @return hover action
	 */
	public HoverAction getHoverAction() {
		return hoverAction;
	}

	/**
	 * Returns hover value or null if not set
	 * @return hover value
	 */
	public String getHoverValue() {
		return hoverValue;
	}

	/**
	 * Sets hover action to SHOW_TEXT and text to given value
	 * @param text - text to show
	 * @return self
	 */
	public IChatBaseComponent onHoverShowText(String text) {
		return onHover(HoverAction.SHOW_TEXT, text);
	}

	/**
	 * Sets hover action to SHOW_ITEM and item to given value
	 * @param item - item to show
	 * @return self
	 */
	/*	public IChatBaseComponent onHoverShowItem(ItemStack item) {
		try {
			String pack = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			return onHover(HoverAction.SHOW_ITEM, Class.forName("net.minecraft.server." + pack + ".ItemStack")
					.getMethod("save", Class.forName("net.minecraft.server." + pack + ".NBTTagCompound"))
					.invoke(Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack")
							.getMethod("asNMSCopy", ItemStack.class).invoke(null, item), 
							Class.forName("net.minecraft.server." + pack + ".NBTTagCompound")
							.getConstructor().newInstance()).toString());
		} catch (Exception e) {
			e.printStackTrace();
			return this;
		}
	}*/

	/**
	 * Sets hover action to SHOW_ITEM and item to given value
	 * @param item - item to show
	 * @return self
	 */
	public IChatBaseComponent onHoverShowItem(String serializedItem) {
		return onHover(HoverAction.SHOW_ITEM, serializedItem);
	}

	/**
	 * Sets hover action to SHOW_ENTITY and entity data to given values
	 * @param id - entity uuid
	 * @param customname - entity custom name, can be null
	 * @param type - entity type, can be null
	 * @return self
	 */
	public IChatBaseComponent onHoverShowEntity(UUID id, String customname, String type) {
		JSONObject json = new JSONObject();
		json.put("id", id.toString());
		if (type != null) json.put("type", type);
		if (customname != null) json.put("name", customname);
		return onHover(HoverAction.SHOW_ENTITY, json.toString());
	}

	/**
	 * Sets hover action and value to given values
	 * @param action - action to perform on hover
	 * @param value - value to perform action with
	 * @return self
	 */
	private IChatBaseComponent onHover(HoverAction action, String value) {
		hoverAction = action;
		hoverValue = value;
		JSONObject hover = new JSONObject();
		hover.put("action", action.toString().toLowerCase());
		hover.put("value", value);
		jsonObject.put("hoverEvent", hover);
		return this;
	}

	/**
	 * Deserializes string and returns created component
	 * @param json - serialized string
	 * @return Deserialized component
	 */
	public static IChatBaseComponent fromString(String json) {
		try {
			if (json == null) return null;
			if (json.startsWith("\"") && json.endsWith("\"")) {
				//simple component with only text used, minecraft serializer outputs the text in quotes instead of full json
				return new IChatBaseComponent(json.substring(1, json.length()-1));
			}
			JSONObject jsonObject = ((JSONObject) new JSONParser().parse(json));
			IChatBaseComponent component = new IChatBaseComponent();
			component.setText((String) jsonObject.get("text"));
			component.setBold(getBoolean(jsonObject, "bold"));
			component.setItalic(getBoolean(jsonObject, "italic"));
			component.setUnderlined(getBoolean(jsonObject, "underlined"));
			component.setStrikethrough(getBoolean(jsonObject, "strikethrough"));
			component.setObfuscated(getBoolean(jsonObject, "obfuscated"));
			component.setColor(TextColor.fromString(((String) jsonObject.get("color"))));
			if (jsonObject.containsKey("clickEvent")) {
				JSONObject clickEvent = (JSONObject) jsonObject.get("clickEvent");
				String action = (String) clickEvent.get("action");
				Object value = (Object) clickEvent.get("value");
				component.onClick(ClickAction.valueOf(action.toUpperCase()), value);
			}
			if (jsonObject.containsKey("hoverEvent")) {
				JSONObject hoverEvent = (JSONObject) jsonObject.get("hoverEvent");
				String action = (String) hoverEvent.get("action");
				String value = (String) hoverEvent.get("value");
				component.onHover(HoverAction.valueOf(action.toUpperCase()), value);
			}
			if (jsonObject.containsKey("extra")) {
				List<Object> list = (List<Object>) jsonObject.get("extra");
				for (Object extra : list) {
					component.addExtra(fromString(extra.toString()));
				}
			}
			return component;
		} catch (ParseException e) {
			TAB.getInstance().debug("Failed to parse json object: " + json);
			return fromColoredText(json);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to read component: " + json, e);
			return fromColoredText(json);
		}
	}

	/**
	 * Returns boolean value of requested key
	 * @param jsonObject - object to get value from
	 * @param key - name of key
	 * @return true if json object contains key and value is true, false otherwise
	 */
	private static Boolean getBoolean(JSONObject jsonObject, String key) {
		if (jsonObject.containsKey(key)) {
			return Boolean.parseBoolean(String.valueOf(jsonObject.get(key)));
		}
		return null;
	}

	/**
	 * Serializes this component with colors based on client version
	 * @param clientVersion - client version
	 * @return Serialized string
	 */
	public String toString(ProtocolVersion clientVersion) {
		return toString(clientVersion, false);
	}

	/**
	 * Serializes this component with colors based on client version
	 * @param clientVersion - client version
	 * @param sendTranslatableIfEmpty - if empty translatable should be sent if text is empty or not
	 * @return Serialized string
	 */
	public String toString(ProtocolVersion clientVersion, boolean sendTranslatableIfEmpty) {
		if (extra == null) {
			if (text == null) return null;
			if (text.length() == 0) {
				if (sendTranslatableIfEmpty) {
					return EMPTY_TRANSLATABLE;
				} else {
					return EMPTY_TEXT;
				}
			}
		}
		//the core component, fixing all colors
		if (color != null) {
			jsonObject.put("color", color.toString(clientVersion.getMinorVersion() >= 16));
		}
		if (extra != null) {
			for (IChatBaseComponent extra : extra) {
				if (extra.color != null) {
					extra.jsonObject.put("color", extra.color.toString(clientVersion.getMinorVersion() >= 16));
				}
			}
		}
		return toString();
	}

	@Override
	public String toString() {
		return jsonObject.toString();
	}

	/**
	 * Returns organized component from colored text
	 * @param originalText - text to convert
	 * @return organized component from colored text
	 */
	public static IChatBaseComponent fromColoredText(String originalText){
		String text = TAB.getInstance().getPlaceholderManager().color(originalText);
		if ((boolean) TAB.getInstance().getConfiguration().getSecretOption("rgb-support", true)) {
			text = rgb.applyFormats(text);
		}
		List<IChatBaseComponent> components = new ArrayList<IChatBaseComponent>();
		StringBuilder builder = new StringBuilder();
		IChatBaseComponent component = new IChatBaseComponent();
		for (int i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if (c == '\u00a7'){
				i++;
				if (i >= text.length()) {
					break;
				}
				c = text.charAt(i);
				if ((c >= 'A') && (c <= 'Z')) {
					c = (char)(c + ' ');
				}
				EnumChatFormat format = EnumChatFormat.getByChar(c);
				if (format != null){
					if (builder.length() > 0) {
						component.setText(builder.toString());
						components.add(component);
						component = component.copyFormatting();
						builder = new StringBuilder();
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
						component = new IChatBaseComponent();
						component.setColor(new TextColor(EnumChatFormat.WHITE));
						break;
					default:
						component = new IChatBaseComponent();
						component.setColor(new TextColor(format));
						break;
					}
				}
			} else if ((boolean) TAB.getInstance().getConfiguration().getSecretOption("rgb-support", true) && c == '#'){
				try {
					String hex = text.substring(i+1, i+7);
					TextColor color = new TextColor(hex); //the validation check is in constructor

					if (builder.length() > 0){
						component.setText(builder.toString());
						components.add(component);
						builder = new StringBuilder();
					}
					component = new IChatBaseComponent();
					component.setColor(color);
					i += 6;
				} catch (Exception e) {
					//invalid hex code
					builder.append(c);
				}
			} else {
				builder.append(c);
			}
		}
		component.setText(builder.toString());
		components.add(component);
		return new IChatBaseComponent("").setExtra(components);
	}

	/**
	 * Returns a new component with identical color and magic codes as current one
	 * @return New component with identical formatting
	 */
	public IChatBaseComponent copyFormatting() {
		IChatBaseComponent component = new IChatBaseComponent();
		component.setBold(bold);
		component.setColor(color);
		component.setItalic(italic);
		component.setObfuscated(obfuscated);
		component.setStrikethrough(strikethrough);
		component.setUnderlined(underlined);
		return component;
	}

	/**
	 * Converts this component into a simple text with legacy colors (closest match if color is set to RGB)
	 * @return The simple text format
	 */
	public String toLegacyText() {
		StringBuilder builder = new StringBuilder();
		append(builder, "");
		return builder.toString();
	}

	/**
	 * Appends text to string builder, might also add color and magic codes if different
	 * than previous component in chain
	 * @param builder - builder to append text to
	 * @param previousFormatting - colors and magic codes in previous component
	 * @return New formatting, might be identical to previous one
	 */
	private String append(StringBuilder builder, String previousFormatting) {
		String formatting = previousFormatting;
		if (text != null) {
			formatting = getFormatting();
			if (!formatting.equals(previousFormatting)) {
				builder.append(formatting);
			}
			builder.append(text);

		}
		if (extra != null)
			for (IChatBaseComponent component : extra) {
				formatting = component.append(builder, formatting);
			}
		return formatting;
	}

	/**
	 * Returns colors and magic codes of this component
	 * @return used colors and magic codes
	 */
	private String getFormatting() {
		StringBuilder builder = new StringBuilder();
		if (color != null) {
			if (color.getLegacyColor() == EnumChatFormat.WHITE) {
				//preventing unwanted &r -> &f conversion and stopping the <1.13 client bug fix from working
				builder.append(EnumChatFormat.RESET.getFormat());
			} else {
				builder.append(color.getLegacyColor().getFormat());
			}
		}
		if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
		if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
		if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
		if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
		if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
		return builder.toString();
	}

	/**
	 * Returns raw text without colors, only works correctly when component is organized
	 * @return raw text in this component
	 */
	public String toRawText() {
		StringBuilder builder = new StringBuilder();
		if (text != null) builder.append(text);
		if (extra != null) {
			for (IChatBaseComponent extra : extra) {
				if (extra.text != null) builder.append(extra.text);
			}
		}
		return builder.toString();
	}

	/**
	 * Returns the most optimized component based on text. Returns null if text is null,
	 * organized component if RGB colors are used or simple component with only text field
	 * containing the whole text when no RGB colors are used
	 * @param text - text to create component from
	 * @return The most performance-optimized component based on text
	 */
	public static IChatBaseComponent optimizedComponent(String text){
		if (text == null) return null;
		if (text.contains("#") || text.contains("&x") || text.contains('\u00a7' + "x")){
			//contains RGB colors
			return IChatBaseComponent.fromColoredText(text);
		} else {
			//no RGB
			return new IChatBaseComponent(text);
		}
	}

	/**
	 * Enum for all possible click actions
	 */
	public enum ClickAction {
		OPEN_URL,
		@Deprecated OPEN_FILE,//Cannot be sent by server
		RUN_COMMAND,
		@Deprecated TWITCH_USER_INFO, //Removed in 1.9
		CHANGE_PAGE,
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //since 1.15
	}

	/**
	 * Enum for all possible hover actions
	 */
	public enum HoverAction {
		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY,
		@Deprecated SHOW_ACHIEVEMENT;//Removed in 1.12
	}
}