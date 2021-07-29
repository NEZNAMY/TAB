package me.neznamy.tab.api.chat.rgb;

public class ChatHoverable {

	private EnumHoverAction action;
	private Object value;
	
	public ChatHoverable(EnumHoverAction action, Object value) {
		this.action = action;
		this.value = value;
	}
	
	public EnumHoverAction getAction() {
		return action;
	}

	public void setAction(EnumHoverAction action) {
		this.action = action;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Enum for all possible hover actions
	 */
	public enum EnumHoverAction {

		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY;

		public static EnumHoverAction fromString(String s) {
			for (EnumHoverAction action : values()) {
				if (s.toUpperCase().contains(action.toString())) return action;
			}
			throw new IllegalArgumentException("HoverAction not found by name " + s);
		}
	}
}
