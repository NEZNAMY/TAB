package me.neznamy.tab.api.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Class for hover event action in chat component
 */
@Data
public class ChatHoverable {

    /** Hover action */
    @NonNull private final EnumHoverAction action;

    /** Hover value */
    @NonNull private final IChatBaseComponent value;

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