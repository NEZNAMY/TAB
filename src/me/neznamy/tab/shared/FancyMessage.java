package me.neznamy.tab.shared;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FancyMessage {

	private List<Extra> extras = Lists.newArrayList();
	
	public FancyMessage() {
	}
	public void add(Extra c) {
		extras.add(c);
	}
	public String toString() {
		if (ProtocolVersion.packageName != null && ProtocolVersion.packageName.equals("v1_8_R1")) return null;
		JsonObject main = new JsonObject();
		main.addProperty("text", "");
		if (!extras.isEmpty()) {
			JsonArray list = new JsonArray();
			for (Extra c : extras) {
				list.add(c.toJSON());
			}
			main.add("extra", list);
		}
		return main.toString();
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
		public JsonObject toJSON() {
			JsonObject obj = new JsonObject();
			obj.addProperty("text", text);
			if (hover != null) {
				JsonObject o = new JsonObject();
				o.addProperty("action", hover.toString());
				o.addProperty("value", hoverValue);
				obj.add("hoverEvent", o);
			}
			if (click != null) {
				JsonObject o = new JsonObject();
				o.addProperty("action", click.toString());
				o.addProperty("value", clickValue);
				obj.add("clickEvent", o);
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

		OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND,CHANGE_PAGE;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}
}