package me.neznamy.tab.shared.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import org.json.simple.JSONObject;

/**
 * Class for hover event action in chat component
 */
@Data
public class ChatHoverable {

    /** Hover action */
    @NonNull private final EnumHoverAction action;

    /** Hover value */
    @NonNull private final IChatBaseComponent value;

    @SuppressWarnings("unchecked")
    public JSONObject serialize() {
        JSONObject hover = new JSONObject();
        hover.put("action", action.toString().toLowerCase());
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 16) {
            hover.put(action.getPreferredKey(), value);
        } else {
            hover.put("value", TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9 ?
                    value : value.toRawText());
        }
        return hover;
    }

    /**
     * Enum for all possible hover actions
     */
    @AllArgsConstructor
    public enum EnumHoverAction {

        SHOW_TEXT("contents"),
        SHOW_ITEM("value"),
        SHOW_ENTITY("contents");

        @Getter private final String preferredKey;
    }
}