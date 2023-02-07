package me.neznamy.tab.api.chat;

import lombok.Data;
import lombok.NonNull;

/**
 * Class for click event action in chat component
 */
@Data
public class ChatClickable {

    /** Click action */
    @NonNull private final EnumClickAction action;

    /** Click value */
    @NonNull private final String value;

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
