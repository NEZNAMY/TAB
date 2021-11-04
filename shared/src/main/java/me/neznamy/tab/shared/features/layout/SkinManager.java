package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SkinManager {

	private ConfigurationFile cache;
	private Map<String, List<String>> players;
	private Map<Integer, List<String>> mineskin;
	private Map<String, List<String>> textures;
	private final List<String> invalidSkins = new ArrayList<>();
	private Object defaultSkin;

	public SkinManager(String defaultSkin) {
		try {
			File f = new File(TAB.getInstance().getPlatform().getDataFolder(), "skincache.yml");
			if (f.exists() || f.createNewFile()) {
				cache = new YamlConfigurationFile(null, f);
				players = cache.getConfigurationSection("players");
				mineskin = cache.getConfigurationSection("mineskin");
				textures = cache.getConfigurationSection("textures");
				this.defaultSkin = getSkin(defaultSkin);
			} else {
				TAB.getInstance().getErrorManager().criticalError("Failed to load skin cache", null);
			}
		} catch (IOException e) {
			TAB.getInstance().getErrorManager().criticalError("Failed to load skin cache", e);
		}
	}

	public Object getSkin(String skin) {
		if (invalidSkins.contains(skin)) return defaultSkin;
		if (skin.startsWith("player:")) {
			String playername = skin.substring(7);
			if (players.containsKey(playername)) {
				return TAB.getInstance().getPlatform().getSkin(players.get(playername));
			}
			List<String> properties = downloadPlayer(playername);
			if (!properties.isEmpty()) {
				players.put(playername, properties);
				cache.set("players", players);
				return TAB.getInstance().getPlatform().getSkin(properties);
			}
			invalidSkins.add(skin);
			return defaultSkin;
		}
		if (skin.startsWith("mineskin:")) {
			int id = Integer.parseInt(skin.substring(9));
			if (mineskin.containsKey(id)) {
				return TAB.getInstance().getPlatform().getSkin(mineskin.get(id));
			}
			List<String> properties = downloadMineskin(id);
			if (!properties.isEmpty()) {
				mineskin.put(id, properties);
				cache.set("mineskin", mineskin);
				return TAB.getInstance().getPlatform().getSkin(properties);
			}
			invalidSkins.add(skin);
			return defaultSkin;
		}
		if (skin.startsWith("texture:")) {
			String texture = skin.substring(9);
			if (textures.containsKey(texture)) {
				return TAB.getInstance().getPlatform().getSkin(textures.get(texture));
			}
			List<String> properties = downloadTexture(texture);
			if (!properties.isEmpty()) {
				textures.put(texture, properties);
				cache.set("textures", textures);
				return TAB.getInstance().getPlatform().getSkin(properties);
			}
			invalidSkins.add(skin);
			return defaultSkin;
		}
		TAB.getInstance().getErrorManager().startupWarn("Invalid skin definition: \"" + skin + "\"");
		return null;
	}

	private List<String> downloadPlayer(String name) {
		try {
			JSONObject json = getResponse("https://api.ashcon.app/mojang/v2/user/" + name);
			JSONObject textures = (JSONObject) json.get("textures");
			JSONObject raw = (JSONObject) textures.get("raw");
			String value = (String) raw.get("value");
			String signature = (String) raw.get("signature");
			return Arrays.asList(value, signature);
		} catch (FileNotFoundException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by player: No user with the name '" + name + "' was found");
			return new ArrayList<>();
		} catch (IOException | ParseException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by player: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	public List<String> downloadMineskin(int id) {
		try {
			JSONObject json = getResponse("https://api.mineskin.org/get/id/" + id);
			JSONObject data = (JSONObject) json.get("data");
			JSONObject texture = (JSONObject) data.get("texture");
			String value = (String) texture.get("value");
			String signature = (String) texture.get("signature");
			return Arrays.asList(value, signature);
		} catch (FileNotFoundException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by id: No skin with the id '" + id + "' was found");
			return new ArrayList<>();
		} catch (IOException | ParseException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by id: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	public List<String> downloadTexture(String texture) {
		try {
			URL url = new URL("https://api.mineskin.org/generate/url/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", "ExampleApp/v1.0");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			String jsonInputString = "{\"variant\":\"classic\",\"name\":\"string\",\"visibility\":0,\"url\":\"http://textures.minecraft.net/texture/" + texture + "\"}";
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			InputStreamReader reader = new InputStreamReader(con.getInputStream());
			JSONObject json = (JSONObject) new JSONParser().parse(reader);
			JSONObject data = (JSONObject) json.get("data");
			JSONObject texture2 = (JSONObject) data.get("texture");
			String value = (String) texture2.get("value");
			String signature = (String) texture2.get("signature");
			return Arrays.asList(value, signature);
		} catch (IOException | ParseException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by texture: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	private JSONObject getResponse(String url) throws IOException, ParseException {
		try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())){
			return (JSONObject) new JSONParser().parse(reader);
		}
	}

	public Object getDefaultSkin() {
		return defaultSkin;
	}
}