package me.neznamy.tab.api.chat;

import java.util.UUID;

import com.google.gson.JsonObject;

public class ChatComponentEntity extends IChatBaseComponent {

	private final String type;
	private final UUID id;
	private final String name;
	
	public ChatComponentEntity(String type, UUID id, String name) {
		this.type = type;
		this.id = id;
		this.name = name;
	}

	@Override
	public String toRawText() {
		return toString();
	}
	
	@Override
	public String toString() {
		return String.format("{\"type\":\"%s\",\"id\":\"%s\",\"name\":{\"text\":\"%s\"}}", type, id, name);
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		json.addProperty("id", id.toString());
		JsonObject json2 = new JsonObject();
		json2.addProperty("text", name);
		json.add("name", json2);
		return json;
	}
}
