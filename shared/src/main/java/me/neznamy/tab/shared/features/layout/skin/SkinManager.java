package me.neznamy.tab.shared.features.layout.skin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.shared.TAB;

public class SkinManager {

    private final List<String> invalidSkins = new ArrayList<>();
    @Getter private Skin defaultSkin;
    private final Map<String, SkinSource> sources = new HashMap<>();

    public SkinManager(String defaultSkin) {
        try {
            File f = new File(TAB.getInstance().getDataFolder(), "skincache.yml");
            if (f.exists() || f.createNewFile()) {
                ConfigurationFile cache = new YamlConfigurationFile(null, f);
                sources.put("player", new PlayerSkin(cache));
                sources.put("mineskin", new MineSkin(cache));
                sources.put("texture", new Texture(cache));
                this.defaultSkin = getSkin(defaultSkin);
            } else {
                TAB.getInstance().getErrorManager().criticalError("Failed to load skin cache", null);
            }
        } catch (IOException e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to load skin cache", e);
        }
    }

    public Skin getSkin(String skin) {
        if (invalidSkins.contains(skin)) return defaultSkin;
        for (Entry<String, SkinSource> entry : sources.entrySet()) {
            if (skin.startsWith(entry.getKey() + ":")) {
                List<String> value = entry.getValue().getSkin(skin.substring(entry.getKey().length()+1));
                if (value.isEmpty()) {
                    invalidSkins.add(skin);
                    return defaultSkin;
                }
                return new Skin(value.get(0), value.get(1));
            }
        }
        TAB.getInstance().getErrorManager().startupWarn("Invalid skin definition: \"" + skin + "\"");
        return null;
    }
}