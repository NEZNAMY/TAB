package me.neznamy.tab.shared.features.layout.skin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class PlayerSkin extends SkinSource {

	protected PlayerSkin(ConfigurationFile file) {
		super(file, "players");
	}

	@Override
	public List<String> download(String input) {
		try {
			JSONObject json = getResponse("https://api.ashcon.app/mojang/v2/user/" + input);
			JSONObject textures = (JSONObject) json.get("textures");
			JSONObject raw = (JSONObject) textures.get("raw");
			String value = (String) raw.get("value");
			String signature = (String) raw.get("signature");
			return Arrays.asList(value, signature);
		} catch (FileNotFoundException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by player: No user with the name '" + input + "' was found");
			return new ArrayList<>();
		} catch (IOException | ParseException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by player: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}
}