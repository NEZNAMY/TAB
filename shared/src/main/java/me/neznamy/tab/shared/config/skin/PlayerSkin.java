package me.neznamy.tab.shared.config.skin;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList.Skin;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Skin source using player names.
 */
public class PlayerSkin extends SkinSource {

    protected PlayerSkin(@NotNull ConfigurationFile file) {
        super(file, "players");
    }

    @Override
    @Nullable
    public Skin download(@NotNull String input) {
        try {
            TabPlayer player = TAB.getInstance().getPlayer(input);
            if (player != null) {
                Skin skin = player.getTabList().getSkin();
                if (skin != null) {
                    TAB.getInstance().debug("Skipping downloading of PLAYER skin " + input + ", because such player is online. Taking skin from their profile instead.");
                    return skin;
                }
            }
            long time = System.currentTimeMillis();
            JSONObject json = getResponse("https://api.ashcon.app/mojang/v2/user/" + input);
            JSONObject textures = (JSONObject) json.get("textures");
            JSONObject raw = (JSONObject) textures.get("raw");
            String value = (String) raw.get("value");
            String signature = (String) raw.get("signature");
            TAB.getInstance().debug("Downloaded PLAYER skin " + input + " in " + (System.currentTimeMillis()-time) + "ms");
            return new Skin(value, signature);
        } catch (FileNotFoundException e) {
            TAB.getInstance().getConfigHelper().runtime().unknownPlayerSkin(input);
        } catch (IOException | ParseException e) {
            TAB.getInstance().getErrorManager().playerSkinDownloadError(input, e);
        }
        return null;
    }
}