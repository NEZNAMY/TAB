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

	//empty translatable
	private static final String EMPTY_TRANSLATABLE = "{\"translate\":\"\"}";

	//empty text
	private static final String EMPTY_TEXT = "{\"text\":\"\"}";

	//component text
	private String text;

	//component color
	private TextColor color;

	//bold flag
	private Boolean bold;

	//italic flag
	private Boolean italic;

	//underlines flag
	private Boolean underlined;

	//strikethrough flag
	private Boolean strikethrough;

	//obfuscated flag
	private Boolean obfuscated;

	//click action
	private ClickAction clickAction;

	//value on click
	private String clickValue;

	//hover action
	private HoverAction hoverAction;

	//value on hover
	private Object hoverValue;

	//extra components
	private List<IChatBaseComponent> extra;

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
		this.text = text;
	}

	/**
	 * Returns list of extra components or null if none are defined
	 * @return list of extras
	 */
	public List<IChatBaseComponent> getExtra(){
		if (extra == null) return new ArrayList<>();
		return extra;
	}

	/**
	 * Sets full list of extra components to given list
	 * @param components - components to use as extra
	 * @return self
	 */
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
		this.extra = components;
		return this;
	}

	/**
	 * Appends provided component as extra component
	 * @param child - component to append
	 * @return self
	 */
	public IChatBaseComponent addExtra(IChatBaseComponent child) {
		if (extra == null) extra = new ArrayList<>();
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
		return bold == Boolean.TRUE;
	}

	/**
	 * Returns true if italic is defined and set to true, false otherwise
	 * @return true if italic is defined and set to true, false otherwise
	 */
	public boolean isItalic(){
		return italic == Boolean.TRUE;
	}

	/**
	 * Returns true if underlined is defined and set to true, false otherwise
	 * @return true if underlined is defined and set to true, false otherwise
	 */
	public boolean isUnderlined(){
		return underlined == Boolean.TRUE;
	}

	/**
	 * Returns true if strikethrough is defined and set to true, false otherwise
	 * @return true if strikethrough is defined and set to true, false otherwise
	 */
	public boolean isStrikethrough(){
		return strikethrough == Boolean.TRUE;
	}

	/**
	 * Returns true if obfuscation is defined and set to true, false otherwise
	 * @return true if obfuscation is defined and set to true, false otherwise
	 */
	public boolean isObfuscated(){
		return obfuscated == Boolean.TRUE;
	}
	
	/**
	 * Returns value of bold
	 * @return value of bold
	 */
	public Boolean getBold(){
		return bold;
	}

	/**
	 * Returns value of italic
	 * @return value of italic
	 */
	public Boolean getItalic(){
		return italic;
	}

	/**
	 * Returns value of underlined
	 * @return value of underlined
	 */
	public Boolean getUnderlined(){
		return underlined;
	}
	
	/**
	 * Returns value of strikethrough
	 * @return value of strikethrough
	 */
	public Boolean getStrikethrough(){
		return strikethrough;
	}

	/**
	 * Returns value of obfuscation
	 * @return value of obfuscation
	 */
	public Boolean getObfuscated(){
		return obfuscated;
	}

	/**
	 * Sets text of this component
	 * @param text - text to show
	 * @return self
	 */
	public IChatBaseComponent setText(String text) {
		this.text = text;
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
		return this;
	}

	/**
	 * Sets italic status to requested value
	 * @param italic - true if italic, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		return this;
	}

	/**
	 * Sets underline status to requested value
	 * @param underlined - true if underlined, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	/**
	 * Sets strikethrough status to requested value
	 * @param strikethrough - true if strikethrough, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		return this;
	}

	/**
	 * Sets obfuscation status to requested value
	 * @param obfuscated - true if obfuscated, false if not, null if not set
	 * @return self
	 */
	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
		this.obfuscated = obfuscated;
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
		return onClick(ClickAction.CHANGE_PAGE, String.valueOf(newpage));
	}

	/**
	 * Sets click action and value to given values
	 * @param action - action to perform on click
	 * @param value - value to perform action with
	 * @return self
	 */
	public IChatBaseComponent onClick(ClickAction action, String value) {
		clickAction = action;
		clickValue = value;
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
	public Object getHoverValue() {
		if (hoverValue instanceof String) return "\"" + hoverValue + "\"";
		return hoverValue;
	}

	/**
	 * Sets hover action to SHOW_TEXT and text to given value
	 * @param text - text to show
	 * @return self
	 */
	public IChatBaseComponent onHoverShowText(String text) {
		return onHoverShowText(IChatBaseComponent.optimizedComponent(text));
	}

	/**
	 * Sets hover action to SHOW_TEXT and text to given value
	 * @param text - text to show
	 * @return self
	 */
	public IChatBaseComponent onHoverShowText(IChatBaseComponent text) {
		return onHover(HoverAction.SHOW_TEXT, text);
	}

	/**
	 * Sets hover action to SHOW_ITEM and item to given value
	 * @param serializedItem - item to show
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
	public IChatBaseComponent onHoverShowEntity(UUID id, String type, String customname) {
		return onHover(HoverAction.SHOW_ENTITY, String.format("{id:%s,type:%s,name:%s}", id, type, customname));
	}

	/**
	 * Sets hover action and value to given values
	 * @param action - action to perform on hover
	 * @param value - value to perform action with
	 * @return self
	 */
	public IChatBaseComponent onHover(HoverAction action, Object value) {
		hoverAction = action;
		hoverValue = value;
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
			if (json.startsWith("\"") && json.endsWith("\"") && json.length() > 1) {
				//simple component with only text used, minecraft serializer outputs the text in quotes instead of full json
				return new IChatBaseComponent(json.substring(1, json.length()-1));
			}
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
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
				String value = clickEvent.get("value").toString();
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
					String string = extra.toString();
					//reverting .toString() removing "" for simple text
					if (!string.startsWith("{")) string = "\"" + string + "\"";
					component.addExtra(fromString(string));
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

	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		if (text != null) json.put("text", text);
		if (color != null) json.put("color", color.toString());
		setMagicCodes(json);
		if (clickAction != null) {
			JSONObject click = new JSONObject();
			click.put("action", clickAction.toString().toLowerCase());
			click.put("value", clickValue);
			json.put("clickEvent", click);
		}
		if (hoverAction != null) {
			JSONObject hover = new JSONObject();
			hover.put("action", hoverAction.toString().toLowerCase());
			hover.put("value", hoverValue);
			json.put("hoverEvent", hover);
		}
		if (extra != null) json.put("extra", extra);
		return json.toString();
	}
	
	private void setMagicCodes(JSONObject json) {
		if (bold != null) json.put("bold", bold);
		if (italic != null) json.put("italic", italic);
		if (underlined != null) json.put("underlined", underlined);
		if (strikethrough != null) json.put("strikethrough", strikethrough);
		if (obfuscated != null) json.put("obfuscated", obfuscated);
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
		if (clientVersion.getMinorVersion() < 16) {
			//core component, fixing all colors
			convertColorsToLegacy();
		}
		return toString();
	}

	/**
	 * Converts all RGB colors everywhere in this component and it's extras into legacy codes
	 */
	private void convertColorsToLegacy() {
		if (color != null) color.setReturnLegacy(true);
		for (IChatBaseComponent child : getExtra()) {
			child.convertColorsToLegacy();
		}
		if (hoverValue instanceof IChatBaseComponent) {
			((IChatBaseComponent)hoverValue).convertColorsToLegacy();
		}
	}

	/**
	 * Returns organized component from colored text
	 * @param originalText - text to convert
	 * @return organized component from colored text
	 */
	public static IChatBaseComponent fromColoredText(String originalText){
		String text = TAB.getInstance().getPlaceholderManager().color(originalText);
		if (TAB.getInstance().getConfiguration().isRgbSupport()) {
			text = RGBUtils.getInstance().applyFormats(text, false);
		}
		List<IChatBaseComponent> components = new ArrayList<>();
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
			} else if (TAB.getInstance().getConfiguration().isRgbSupport() && c == '#'){
				try {
					String hex = text.substring(i, i+7);
					Integer.parseInt(hex.substring(1), 16); //validating code, skipping otherwise
					TextColor color;
					if (containsLegacyCode(text, i)) {
						color = new TextColor(hex, EnumChatFormat.getByChar(text.charAt(i+8)));
						i += 8;
					} else {
						color = new TextColor(hex);
						i += 6;
					}
					if (builder.length() > 0){
						component.setText(builder.toString());
						components.add(component);
						builder = new StringBuilder();
					}
					component = new IChatBaseComponent();
					component.setColor(color);
				} catch (Exception e) {
					//invalid hex code or string index out of bounds
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
	 * Returns true if text contains legacy color request at defined RGB index start
	 * @param text - text to check
	 * @param i - current index start
	 * @return true if legacy color is defined, false if not
	 */
	private static boolean containsLegacyCode(String text, int i) {
		if (text.length() - i < 9 || text.charAt(i+7) != '|') return false;
		return EnumChatFormat.getByChar(text.charAt(i+8)) != null;
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
		for (IChatBaseComponent component : getExtra()) {
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
		appendMagicCodes(builder);
		return builder.toString();
	}

	/**
	 * Returns raw text without colors, only works correctly when component is organized
	 * @return raw text in this component
	 */
	public String toRawText() {
		StringBuilder builder = new StringBuilder();
		if (text != null) builder.append(text);
		for (IChatBaseComponent child : getExtra()) {
			if (child.text != null) builder.append(child.text);
		}
		return builder.toString();
	}

	/**
	 * Converts the component into flat text with used colors (including rgb) and magic codes
	 * @return converted text
	 */
	public String toFlatText() {
		StringBuilder builder = new StringBuilder();
		if (color != null) builder.append(color.getHexCode());
		appendMagicCodes(builder);
		if (text != null) builder.append(text);
		for (IChatBaseComponent child : getExtra()) {
			builder.append(child.toFlatText());
		}
		return builder.toString();
	}
	
	private void appendMagicCodes(StringBuilder builder) {
		if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
		if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
		if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
		if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
		if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
	}

	/**
	 * Creates a complete clone of this component and returns it
	 * @return a clone identical to current component
	 */
	public IChatBaseComponent clone() {
		IChatBaseComponent component = new IChatBaseComponent(text);
		component.setBold(bold);
		component.setColor(color);
		component.setItalic(italic);
		component.setObfuscated(obfuscated);
		component.setStrikethrough(strikethrough);
		component.setUnderlined(underlined);
		if (hoverAction != null) component.onHover(hoverAction, hoverValue);
		if (clickAction != null) component.onClick(clickAction, clickValue);
		for (IChatBaseComponent child : getExtra()) {
			component.addExtra(child.clone());
		}
		return component;
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
		RUN_COMMAND,
		CHANGE_PAGE, //since 1.8
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //since 1.15
	}

	/**
	 * Enum for all possible hover actions
	 */
	public enum HoverAction {
		
		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY;
		
		public static HoverAction fromString(String s) {
			for (HoverAction action : values()) {
				if (s.toUpperCase().contains(action.toString())) return action;
			}
			throw new IllegalArgumentException("HoverAction not found by name " + s);
		}
	}
}