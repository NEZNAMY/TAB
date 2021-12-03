package me.neznamy.tab.api.chat;

import me.neznamy.tab.api.util.Preconditions;

/**
 * Class for click event action in chat component
 */
public class ChatClickable {

	/** Click action */
	private final EnumClickAction action;

	/** Click value */
	private final String value;

	/**
	 * Constructs new instance with given action and value.
	 * @param	action
	 * 			click event action
	 * @param	value
	 * 			click event value
	 * @throws	IllegalArgumentException
	 * 			if {@code action} is null or {@code value} is null
	 */
	public ChatClickable(EnumClickAction action, String value) {
		Preconditions.checkNotNull(action, "hover action cannot be null");
		Preconditions.checkNotNull(value, "hover value cannot be null");
		this.action = action;
		this.value = value;
	}

	/**
	 * Returns click action defined in constructor
	 * @return	click action
	 */
	public EnumClickAction getAction() {
		return action;
	}

	/**
	 * Returns click action defined in constructor
	 * @return	click action
	 */
	public String getValue() {
		return value;
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
