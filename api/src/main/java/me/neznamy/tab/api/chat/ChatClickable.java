package me.neznamy.tab.api.chat;

public class ChatClickable {

	private EnumClickAction action;
	private String value;

	public ChatClickable(EnumClickAction action, String value) {
		this.action = action;
		this.value = value;
	}

	public EnumClickAction getAction() {
		return action;
	}

	public void setAction(EnumClickAction action) {
		this.action = action;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Enum for all possible click actions
	 */
	public enum EnumClickAction {
		
		OPEN_URL,
		RUN_COMMAND,
		CHANGE_PAGE, //since 1.8
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //since 1.15
	}
}
