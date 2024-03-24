package me.neznamy.tab.shared.features.layout.skin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.TAB;

/**
 * Skin source using mineskin.org for skins.
 */
public class MineSkin extends SkinSource {

    protected MineSkin(@NotNull ConfigurationFile file) {
        super(file, "mineskin");
    }

    @Override
    @NotNull
    public List<String> download(@NotNull String input) {
        try {
            String type;
            try {
                Integer.parseInt(input);
                type = input.length() < 20 ? "id" : "uuid";
            } catch (NumberFormatException ex) {
                type = "uuid";
            }
            JSONObject json = getResponse("https://api.mineskin.org/get/" + type + "/" + input);
            JSONObject data = (JSONObject) json.get("data");
            JSONObject texture = (JSONObject) data.get("texture");
            String value = (String) texture.get("value");
            String signature = (String) texture.get("signature");
            return Arrays.asList(value, signature);
        } catch (FileNotFoundException e) {
            TAB.getInstance().getConfigHelper().runtime().unknownMineSkin(input);
        } catch (IOException | ParseException e) {
            TAB.getInstance().getErrorManager().mineSkinDownloadError(input, e);
        }
        return Collections.emptyList();
    }
}