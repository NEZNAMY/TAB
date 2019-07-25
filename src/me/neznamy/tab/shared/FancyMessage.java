package me.neznamy.tab.shared;

import java.util.List;

import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

public class FancyMessage {

	private List<Extra> extras = Lists.newArrayList();
	
	public FancyMessage() {
	}
	public void add(Extra c) {
		extras.add(c);
	}
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject main = new JSONObject();
		main.put("text", "");
		if (!extras.isEmpty()) {
			List<JSONObject> list = Lists.newArrayList();
			for (Extra c : extras) {
				list.add(c.toJSON());
			}
			main.put("extra", list);
		}
		return main.toString();
	}
	public static class Extra {
		
		private String text;
		private HoverAction hover;
		private String hoverValue;
		private ClickAction click;
		private String clickValue;
		
		public Extra(String...text) {
			String txt = "";
			for (int i=0; i<text.length; i++) {
				if (i>0) txt += "\n";
				txt += text[i];
			}
			this.text = txt;
		}
		public Extra onHover(HoverAction action, String... value) {
			hover = action;
			String txt = "";
			for (int i=0; i<value.length; i++) {
				if (i>0) txt += "\n";
				txt += value[i];
			}
			this.hoverValue = txt;
			return this;
		}
		public Extra onClick(ClickAction action, String... value) {
			click = action;
			String txt = "";
			for (int i=0; i<value.length; i++) {
				if (i>0) txt += "\n";
				txt += value[i];
			}
			this.clickValue = txt;
			return this;
		}
		@SuppressWarnings("unchecked")
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			obj.put("text", text);
			if (hover != null) {
				JSONObject hoverevent = new JSONObject();
				hoverevent.put("action", hover.get());
				hoverevent.put("value", hoverValue);
				obj.put("hoverEvent", hoverevent);
			}
			if (click != null) {
				JSONObject clickevent = new JSONObject();
				clickevent.put("action", click.get());
				clickevent.put("value", clickValue);
				obj.put("clickEvent", clickevent);
			}
			return obj;
		}
	}
	public enum HoverAction{
		
//		SHOW_ACHIEVEMENT("show_achievement"),
		SHOW_TEXT("show_text"),
		SHOW_ITEM("show_item"),
		SHOW_ENTITY("show_entity");
		
		private String string;
		
		private HoverAction(String string) {
			this.string = string;
		}
		public String get() {
			return string;
		}
	}
	public enum ClickAction{
		
		OPEN_URL("open_url"),
		OPEN_FILE("open_file"),
		RUN_COMMAND("run_command"),
		SUGGEST_COMMAND("suggest_command"),
		CHANGE_PAGE("change_page");
		
		private String string;
		
		private ClickAction(String string) {
			this.string = string;
		}
		public String get() {
			return string;
		}
	}
}