package me.neznamy.tab.shared.features.layout.skin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class Texture extends SkinSource {

	protected Texture(ConfigurationFile file) {
		super(file, "textures");
	}

	@Override
	public List<String> download(String texture) {
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
}