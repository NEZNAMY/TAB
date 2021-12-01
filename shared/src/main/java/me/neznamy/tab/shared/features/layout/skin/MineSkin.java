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

public class MineSkin extends SkinSource {

	protected MineSkin(ConfigurationFile file) {
		super(file, "mineskin");
	}

	@Override
	public List<String> download(String input) {
		try {
			JSONObject json = getResponse("https://api.mineskin.org/get/id/" + input);
			JSONObject data = (JSONObject) json.get("data");
			JSONObject texture = (JSONObject) data.get("texture");
			String value = (String) texture.get("value");
			String signature = (String) texture.get("signature");
			return Arrays.asList(value, signature);
		} catch (FileNotFoundException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by id: No skin with the id '" + input + "' was found");
			return new ArrayList<>();
		} catch (IOException | ParseException e) {
			TAB.getInstance().getErrorManager().printError("Failed to load skin by id: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}
}