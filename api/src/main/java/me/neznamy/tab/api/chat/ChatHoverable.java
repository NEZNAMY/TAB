package me.neznamy.tab.api.chat;

public class ChatHoverable {

	private EnumHoverAction action;
	private IChatBaseComponent value;
	
	public ChatHoverable(EnumHoverAction action, IChatBaseComponent value) {
		this.action = action;
		this.value = value;
	}
	
	public EnumHoverAction getAction() {
		return action;
	}

	public void setAction(EnumHoverAction action) {
		this.action = action;
	}

	public IChatBaseComponent getValue() {
		return value;
	}

	public void setValue(IChatBaseComponent value) {
		this.value = value;
	}

	/**
	 * Enum for all possible hover actions
	 */
	public enum EnumHoverAction {

		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY;
	}
}
