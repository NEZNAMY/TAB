package me.neznamy.tab.api.chat;

import me.neznamy.tab.api.util.Preconditions;

/**
 * Class for hover event action in chat component
 */
public class ChatHoverable {

    /** Hover action */
    private final EnumHoverAction action;

    /** Hover value */
    private final IChatBaseComponent value;

    /**
     * Constructs new instance with given action and value.
     *
     * @param   action
     *          hover event action
     * @param   value
     *          hover event value
     * @throws  IllegalArgumentException
     *          if {@code action} is null or {@code value} is null
     */
    public ChatHoverable(EnumHoverAction action, IChatBaseComponent value) {
        Preconditions.checkNotNull(action, "hover action");
        Preconditions.checkNotNull(value, "hover value");
        this.action = action;
        this.value = value;
    }

    /**
     * Returns hover action defined in constructor
     *
     * @return  hover action
     */
    public EnumHoverAction getAction() {
        return action;
    }

    /**
     * Returns hover value defined in constructor
     *
     * @return  hover value
     */
    public IChatBaseComponent getValue() {
        return value;
    }

    /**
     * Enum for all possible hover actions
     */
    public enum EnumHoverAction {

        SHOW_TEXT("contents"),
        SHOW_ITEM("value"),
        SHOW_ENTITY("contents");

        private final String preferredKey;

        EnumHoverAction(String preferredKey) {
            this.preferredKey = preferredKey;
        }

        public String getPreferredKey() {
            return preferredKey;
        }
    }
}