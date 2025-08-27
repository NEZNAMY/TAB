package me.neznamy.tab.shared.config.skin;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Skin manager for retrieving player skins from various sources.
 */
public class SkinManager {

    /** Skins defined in configuration that are invalid */
    private final List<String> invalidSkins = new ArrayList<>();

    /** Registered skin patterns and their sources */
    private final Map<String, SkinSource> sources = new HashMap<>();

    /**
     * Constructs new instance with given parameter.
     *
     * @param   skinCache
     *          File containing cached skins
     */
    public SkinManager(@NotNull ConfigurationFile skinCache) {
        sources.put("player", new PlayerSkin(skinCache));
        sources.put("mineskin", new MineSkin(skinCache));
        sources.put("texture", new Texture(skinCache));
        sources.put("signed_texture", new SignedTexture(skinCache));
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
        // Check if this skin is already known as invalid to not send warn or try again
        if (invalidSkins.contains(skin)) return null;

        for (Entry<String, SkinSource> entry : sources.entrySet()) {
            if (skin.startsWith(entry.getKey() + ":")) {
                Skin value = entry.getValue().getSkin(skin.substring(entry.getKey().length() + 1));
                if (value == null) {
                    // Skin source failed to provide a skin, warn and remember as invalid
                    invalidSkins.add(skin);
                    return null;
                }
                return value;
            }
        }

        // Unknown skin type
        TAB.getInstance().getConfigHelper().startup().invalidSkinDefinition(skin);
        invalidSkins.add(skin);
        return null;
    }
}