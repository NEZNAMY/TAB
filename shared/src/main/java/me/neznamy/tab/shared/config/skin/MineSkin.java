package me.neznamy.tab.shared.config.skin;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Skin source using mineskin.org for skins.
 */
public class MineSkin extends SkinSource {

    protected MineSkin(@NotNull ConfigurationFile file) {
        super(file, "mineskin");
    }

    @Override
    @Nullable
    public Skin download(@NotNull String input) {
        try {
            long time = System.currentTimeMillis();
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
            TAB.getInstance().debug("Downloaded MINESKIN skin " + input + " in " + (System.currentTimeMillis()-time) + "ms");
            return new Skin(value, signature);
        } catch (FileNotFoundException e) {
            TAB.getInstance().getConfigHelper().runtime().unknownMineSkin(input);
        } catch (IOException | ParseException e) {
            TAB.getInstance().getErrorManager().mineSkinDownloadError(input, e);
        }
        return null;
    }
}