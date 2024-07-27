package me.neznamy.tab.shared.features.layout.skin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Skin manager for layout feature.
 */
public class SkinManager {

    /** Skins defined in configuration that are invalid */
    private final List<String> invalidSkins = new ArrayList<>();

    /** Configured default skin */
    @Getter private Skin defaultSkin;

    /** Default skins per slot */
    private final Map<Integer, Skin> defaultSkinHashMap = new HashMap<>();

    /** Registered skin patterns and their sources */
    private final Map<String, SkinSource> sources = new HashMap<>();

    /**
     * Constructs new instance with given parameters and loads cache.
     *
     * @param   defaultSkin
     *          Defined default skin
     * @param   defaultSkinHashMap
     *          Map of default skins per slot
     */
    public SkinManager(@NotNull String defaultSkin, @NotNull Map<Integer, String> defaultSkinHashMap) {
        try {
            File f = new File(TAB.getInstance().getDataFolder(), "skincache.yml");
            if (f.exists() || f.createNewFile()) {
                ConfigurationFile cache = new YamlConfigurationFile(null, f);
                sources.put("player", new PlayerSkin(cache));
                sources.put("mineskin", new MineSkin(cache));
                sources.put("texture", new Texture(cache));
                sources.put("signed_texture", new SignedTexture(cache));
                this.defaultSkin = getSkin(defaultSkin);
                for (Map.Entry<Integer, String> entry : defaultSkinHashMap.entrySet()) {
                    Skin skin = getSkin(entry.getValue());
                    if (skin != null) this.defaultSkinHashMap.put(entry.getKey(), skin);
                }
            } else {
                TAB.getInstance().getErrorManager().criticalError("Failed to load skin cache", null);
            }
        } catch (IOException e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to load skin cache", e);
        }
    }

    /**
     * Returns default skin of specified slot. If not defined, default skin is returned.
     *
     * @param   slot
     *          Slot id
     * @return  Default skin for specified slot
     */
    @NotNull
    public Skin getDefaultSkin(int slot) {
        return defaultSkinHashMap.getOrDefault(slot, defaultSkin);
    }

    /**
     * Returns skin from given skin definition. If Skin is invalid, warn is printed and
     * {@code null} is returned.
     *
     * @param   skin
     *          Full skin definition
     * @return  Skin from given definition
     */
    @Nullable
    public Skin getSkin(@NotNull String skin) {
        if (invalidSkins.contains(skin)) return defaultSkin;
        for (Entry<String, SkinSource> entry : sources.entrySet()) {
            if (skin.startsWith(entry.getKey() + ":")) {
                List<String> value = entry.getValue().getSkin(skin.substring(entry.getKey().length() + 1));
                if (value.isEmpty()) {
                    invalidSkins.add(skin);
                    return defaultSkin;
                }
                if (value.size() == 1) {
                    return new Skin(value.get(0), null);
                }
                return new Skin(value.get(0), value.get(1));
            }
        }
        TAB.getInstance().getConfigHelper().startup().invalidLayoutSkinDefinition(skin);
        return null;
    }
}