package me.neznamy.tab.api.chat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

/**
 * A class representing a chat component that was serialized before,
 * but deserialize was requested in PacketPlayOutPlayerInfo packet.
 * This is used to read and modify the packet and then rewrite it.
 * However, display name components must be deserialized to properly
 * forward them in the new packet, but its value is not actually
 * used anywhere.
 * <p>
 * To avoid this heavy and completely unnecessary operation, the original
 * string is saved and returned during serialization. If any read/write
 * operation is performed, the string is fully deserialized and this object
 * will act like a normal chat component.
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class DeserializedChatComponent extends IChatBaseComponent {

    /** The original serialized component string */
    @NonNull private final String json;

    /**
     * Flag tracking whether this component was fully deserialized
     * in case of a read operation to avoid repeating the deserialization
     * process on every request.
     */
    private boolean deserialized;

    /**
     * Flag tracking whether this component was modified, which means
     * a new string has to be created instead of returning the input one.
     */
    private boolean modified;

    @Override
    public String toString() {
        if (modified) return super.toString();
        return json;
    }

    @Override
    public String toString(ProtocolVersion clientVersion) {
        if (modified) return super.toString(clientVersion);
        return json;
    }

    @Override
    public List<IChatBaseComponent> getExtra() {
        if (!deserialized) deserialize();
        return super.getExtra();
    }

    @Override
    public String getText() {
        if (!deserialized) deserialize();
        return super.getText();
    }

    @Override
    public @NotNull ChatModifier getModifier() {
        if (!deserialized) deserialize();
        return super.getModifier();
    }

    @Override
    public IChatBaseComponent setExtra(List<IChatBaseComponent> components) {
        if (!deserialized) deserialize();
        modified = true;
        return super.setExtra(components);
    }

    @Override
    public void addExtra(@NotNull IChatBaseComponent child) {
        if (!deserialized) deserialize();
        modified = true;
        super.addExtra(child);
    }

    @Override
    public void setModifier(@NotNull ChatModifier modifier) {
        if (!deserialized) deserialize();
        modified = true;
        super.setModifier(modifier);
    }

    /**
     * Performs a full deserialization process on this component.
     */
    void deserialize() {
        deserialized = true;
        if (json.startsWith("\"") && json.endsWith("\"") && json.length() > 1) {
            //simple component with only text used, minecraft serializer outputs the text in quotes instead of full json
            setText(json.substring(1, json.length()-1));
            return;
        }
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            TabAPI.getInstance().logError("Failed to deserialize json component " + json, e);
            return;
        }
 /*       if (jsonObject.containsKey("type")) {
            return new ChatComponentEntity((String) jsonObject.get("type"), UUID.fromString((String) jsonObject.get("id")), IChatBaseComponent.deserialize(jsonObject.get("name").toString()).toFlatText());
        }*/
        setText((String) jsonObject.get("text"));
        getModifier().setBold(getBoolean(jsonObject, "bold"));
        getModifier().setItalic(getBoolean(jsonObject, "italic"));
        getModifier().setUnderlined(getBoolean(jsonObject, "underlined"));
        getModifier().setStrikethrough(getBoolean(jsonObject, "strikethrough"));
        getModifier().setObfuscated(getBoolean(jsonObject, "obfuscated"));
        getModifier().setColor(TextColor.fromString(((String) jsonObject.get("color"))));
        if (jsonObject.containsKey("clickEvent")) {
            JSONObject clickEvent = (JSONObject) jsonObject.get("clickEvent");
            String action = (String) clickEvent.get("action");
            String value = clickEvent.get("value").toString();
            getModifier().onClick(ChatClickable.EnumClickAction.valueOf(action.toUpperCase()), value);
        }
        if (jsonObject.containsKey("hoverEvent")) {
            JSONObject hoverEvent = (JSONObject) jsonObject.get("hoverEvent");
            String action = (String) hoverEvent.get("action");
            String value = (String) hoverEvent.get("value");
            getModifier().onHover(ChatHoverable.EnumHoverAction.valueOf(action.toUpperCase()), deserialize(value));
        }
        if (jsonObject.containsKey("extra")) {
            List<Object> list = (List<Object>) jsonObject.get("extra");
            for (Object extra : list) {
                String string = extra.toString();
                //reverting .toString() removing "" for simple text
                if (!string.startsWith("{")) string = "\"" + string + "\"";
                addExtra(IChatBaseComponent.deserialize(string));
            }
        }
    }

    /**
     * Returns boolean value of requested key from map
     *
     * @param   jsonObject
     *          map to get value from
     * @param   key
     *          name of key
     * @return  value from json object or null if not present
     */
    private static Boolean getBoolean(@NonNull JSONObject jsonObject, @NonNull String key) {
        String value = String.valueOf(jsonObject.getOrDefault(key, null));
        return "null".equals(value) ? null : Boolean.parseBoolean(value);
    }
}