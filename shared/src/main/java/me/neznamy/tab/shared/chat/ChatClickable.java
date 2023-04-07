package me.neznamy.tab.shared.chat;

import lombok.Data;
import lombok.NonNull;
import org.json.simple.JSONObject;

/**
 * Class for click event action in chat component
 */
@Data
public class ChatClickable {

    /** Click action */
    @NonNull private final EnumClickAction action;

    /** Click value */
    @NonNull private final String value;

    @SuppressWarnings("unchecked")
    public JSONObject serialize() {
        JSONObject click = new JSONObject();
        click.put("action", action.toString().toLowerCase());
        click.put("value", value);
        return click;
    }

    /**
     * Enum for all possible click actions
     */
    public enum EnumClickAction {

        OPEN_URL,
        RUN_COMMAND,
        CHANGE_PAGE, //since 1.8
        SUGGEST_COMMAND,
        COPY_TO_CLIPBOARD //since 1.15
    }
}
