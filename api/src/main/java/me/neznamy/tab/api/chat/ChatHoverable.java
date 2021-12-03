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
	 * @param	action
	 * 			hover event action
	 * @param	value
	 * 			hover event value
	 * @throws	IllegalArgumentException
	 * 			if {@code action} is null or {@code value} is null
	 */
	public ChatHoverable(EnumHoverAction action, IChatBaseComponent value) {
		Preconditions.checkNotNull(action, "hover action cannot be null");
		Preconditions.checkNotNull(value, "hover value cannot be null");
		this.action = action;
		this.value = value;
	}

	/**
	 * Returns hover action defined in constructor
	 * @return	hover action
	 */
	public EnumHoverAction getAction() {
		return action;
	}

	/**
	 * Returns hover value defined in constructor
	 * @return	hover value
	 */
	public IChatBaseComponent getValue() {
		return value;
	}

	/**
	 * Enum for all possible hover actions
	 */
	public enum EnumHoverAction {

		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY
	}
}