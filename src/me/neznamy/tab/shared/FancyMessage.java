package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class FancyMessage {

	private List<Extra> extras = new ArrayList<Extra>();
	
	public FancyMessage() {
	}
	public void add(Extra c) {
		extras.add(c);
	}
	public String toString() {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
			//1.7+
			JSONObject main = new JSONObject();
			main.put("text", "");
			if (!extras.isEmpty()) {
				JSONArray list = new JSONArray();
				for (Extra c : extras) {
					list.add(c.toJSON());
				}
				main.put("extra", list);
			}
			return main.toString();
		} else {
			//1.5.x, 1.6.x
			String text = "";
			if (!extras.isEmpty()) {
				for (Extra c : extras) {
					text += c.text;
				}
			}
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 6) {
				//1.6.x
				return "{\"text\":\"" + text + "\"}";
			} else {
				//1.5.x
				return text;
			}
		}
	}
	public static class Extra {
		
		private String text;
		private HoverAction hover;
		private String hoverValue;
		private ClickAction click;
		private String clickValue;
		
		public Extra(String... text) {
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
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			obj.put("text", text);
			if (hover != null) {
				JSONObject o = new JSONObject();
				o.put("action", hover.toString());
				o.put("value", hoverValue);
				obj.put("hoverEvent", o);
			}
			if (click != null) {
				JSONObject o = new JSONObject();
				o.put("action", click.toString());
				o.put("value", clickValue);
				obj.put("clickEvent", o);
			}
			return obj;
		}
	}
	public enum HoverAction{

		SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY;
		
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
	public enum ClickAction{

		OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}
}